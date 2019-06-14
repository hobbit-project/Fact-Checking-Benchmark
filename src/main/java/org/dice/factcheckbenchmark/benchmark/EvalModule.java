package org.dice.factcheckbenchmark.benchmark;

import java.io.ByteArrayInputStream;
import java.util.ArrayList;
import java.util.LongSummaryStatistics;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.Literal;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.dice.factcheckbenchmark.benchmark.roc.F1OptimizedResult;
import org.dice.factcheckbenchmark.benchmark.roc.ROCCurve;
import org.dice.factcheckbenchmark.benchmark.roc.ROCEvaluator;
import org.dice.factcheckbenchmark.benchmark.vocab.FactCheck;
import org.hobbit.core.Constants;
import org.hobbit.core.components.AbstractEvaluationModule;
import org.hobbit.core.rabbit.RabbitMQUtils;
import org.hobbit.utils.EnvVariables;
import org.hobbit.utils.rdf.RdfHelper;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class EvalModule extends AbstractEvaluationModule {
    private static final Logger logger = LoggerFactory.getLogger(EvalModule.class);

    // private Model model = ModelFactory.createDefaultModel();

    public static final String TRUE_RESPONSE = "/correct/";
    public static final String FALSE_RESPONSE = "/wrong/";

    // Accumulators to store values to calculate ROC/AUC
    private ArrayList<Long> runTimePerTask = new ArrayList<>();
    private ArrayList<Boolean> trueLabels = new ArrayList<>();
    private ArrayList<Double> confidenceScores = new ArrayList<>();
    private long earliestTaskSent = Long.MAX_VALUE;
    private long latestResponseReceived = 0;

    @Override
    protected void evaluateResponse(byte[] expectedData, byte[] receivedData, long taskSentTimestamp,
            long responseReceivedTimestamp) throws Exception {
        // getROCCurve the given response and store the result, e.g., increment internal
        // counters

        Double receivedScore = null;

        if ((taskSentTimestamp <= 0) || (expectedData == null)) {
            logger.warn("Got a response without expected data. It will be ignored");
            return;
        } else if ((responseReceivedTimestamp <= 0) || (receivedData == null)) {
            logger.debug("Got a task without response data.");
        } else {
            // data has been expected and data has been received.
            runTimePerTask.add(responseReceivedTimestamp - taskSentTimestamp);
            latestResponseReceived = Math.max(responseReceivedTimestamp, latestResponseReceived);

            // parse received response
            try {
                receivedScore = parseResponse(receivedData);
            } catch (Exception e) {
                logger.info("Couldnt parse received truth value as RDF. Trying to parse it as a single double value");
                try {
                    receivedScore = Double.valueOf(RabbitMQUtils.readString(receivedData));
                } catch (Exception e2) {
                    logger.error("Couldnt parse received truth value!", e2);
                }
            }
        }
        earliestTaskSent = Math.min(earliestTaskSent, taskSentTimestamp);

        String expectedResponse = RabbitMQUtils.readString(expectedData);

        // Update accumulators for ROC/AUC calculation
        confidenceScores.add(receivedScore);
        boolean label = expectedResponse.contains(TRUE_RESPONSE);
        trueLabels.add(label);
        logger.debug("True Label {}, Confidence Score {}", label, receivedScore);
    }

    protected double parseResponse(byte[] receivedData) throws Exception {
        Model response = ModelFactory.createDefaultModel();
        response.read(new ByteArrayInputStream(receivedData), null, "TTL");
        Literal literal = RdfHelper.getLiteral(response, null, response.getProperty("http://swc2017.aksw.org/hasTruthValue"));
        return literal.getDouble();
    }

    @Override
    protected Model summarizeEvaluation() throws Exception {
        logger.debug("summarizeEvaluation()");
        // All tasks/responses have been evaluated. Summarize the results,
        // write them into a Jena model and send it to the benchmark controller.

        // Calculate AUC and obtain the points for the ROC curve
        ROCEvaluator evaluator = new ROCEvaluator(trueLabels, confidenceScores);
        ROCCurve rocCurve = evaluator.getROCCurve();
        F1OptimizedResult f1Result = evaluator.calculateOptimizedF1Score();
        
        // Workaround for our test case
        if(experimentUri == null) {
            // Get the experiment URI
            experimentUri = EnvVariables.getString(Constants.HOBBIT_EXPERIMENT_URI_KEY, logger);
        }

        Model model = super.createDefaultModel();

        Resource experimentResource = model.getResource(experimentUri);
        model.add(experimentResource, RDF.type, HOBBIT.Experiment);

        // //Accuracy literal
        // Literal accuracyLiteral = model.createTypedLiteral(accuracy,
        // XSDDatatype.XSDdouble);
        // model.add(experimentResource, FactCheck.accuracy, accuracyLiteral);
        // logger.debug(FactCheck.accuracy.getURI() + " added to model: " + accuracy);

        // AUC literal
        Literal aucLiteral = model.createTypedLiteral(rocCurve.calculateAUC(), XSDDatatype.XSDdouble);
        model.add(experimentResource, FactCheck.auc, aucLiteral);
        logger.debug(FactCheck.auc.getURI() + " added to model: " + rocCurve.calculateAUC());

        // Runtime literal
        if ((latestResponseReceived > 0) && (earliestTaskSent < Long.MAX_VALUE)) {
            long totalRuntime = latestResponseReceived - earliestTaskSent;
            model.add(experimentResource, FactCheck.runTime,
                    model.createTypedLiteral(totalRuntime, XSDDatatype.XSDlong));
            logger.debug(FactCheck.runTime.getURI() + " added to model: " + totalRuntime);
        }

        // Precision literal
        model.add(experimentResource, FactCheck.precision,
                model.createTypedLiteral(f1Result.getPrecision(), XSDDatatype.XSDdouble));
        logger.debug(FactCheck.precision.getURI() + " added to model: " + f1Result.getPrecision());

        // Recall literal
        model.add(experimentResource, FactCheck.recall,
                model.createTypedLiteral(f1Result.getRecall(), XSDDatatype.XSDdouble));
        logger.debug(FactCheck.recall.getURI() + " added to model: " + f1Result.getRecall());

        // F1-measure literal
        model.add(experimentResource, FactCheck.fmeasure,
                model.createTypedLiteral(f1Result.getF1score(), XSDDatatype.XSDdouble));
        logger.debug(FactCheck.fmeasure.getURI() + " added to model: " + f1Result.getF1score());

        // Confidence literal
        model.add(experimentResource, FactCheck.confidenceThreshold,
                model.createTypedLiteral(f1Result.getConfidence(), XSDDatatype.XSDdouble));
        logger.debug(FactCheck.confidenceThreshold.getURI() + " added to model: " + f1Result.getConfidence());

        if (!runTimePerTask.isEmpty()) {
            LongSummaryStatistics stats = runTimePerTask.parallelStream().mapToLong(t -> t).summaryStatistics();
            double avg = stats.getAverage();
            model.add(experimentResource, FactCheck.avgRunTime, model.createTypedLiteral(avg, XSDDatatype.XSDdouble));
            double stdDev = Math.sqrt(runTimePerTask.parallelStream().mapToDouble(t -> t.doubleValue())
                    .map(t -> t - avg).map(t -> t * t).sum());
            stdDev = stdDev / runTimePerTask.size();
            model.add(experimentResource, FactCheck.avgRunTimeStdDev,
                    model.createTypedLiteral(stdDev, XSDDatatype.XSDdouble));
        }

        // ROC curve TODO
        // Literal rocLiteral = model.createTypedLiteral(rocCurve.toString(),
        // XSDDatatype.XSDstring);
        // model.add(experimentResource, , rocLiteral);
        // logger.debug(BenchmarkConstants.ENV_KPI_ROC + " added to model: " +
        // rocCurve.toString());

        return model;
    }

}
