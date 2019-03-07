package org.dice.factcheckbenchmark.benchmark;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.dice.factcheckbenchmark.BenchmarkConstants;
import org.dice.factcheckbenchmark.benchmark.roc.ROCCurve;
import org.dice.factcheckbenchmark.benchmark.roc.ROCEvaluator;
import org.hobbit.core.components.AbstractEvaluationModule;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;

import java.util.ArrayList;
import java.util.Map;


public class EvalModule extends AbstractEvaluationModule {
    private static final Logger logger = LoggerFactory.getLogger(EvalModule.class);

    private double FACTCHECK_THRESHOLD;
    private int truePositive = 0;
    private int falsePositive = 0;
    private int trueNegative = 0;
    private int falseNegative = 0;
    private long totalRunTime = 0;

    private Model model = ModelFactory.createDefaultModel();

    private Property EVAL_ACCURACY = null;
    private Property EVAL_RUN_TIME = null;
    private Property EVAL_AUC = null;
    private Property EVAL_ROC = null;
    private Property EVAL_PRECISION = null;
    private Property EVAL_RECALL = null;

    //Accumulators to store values to calculate ROC/AUC
    private ArrayList<Integer> trueLabels = new ArrayList<>();
    private ArrayList<Double> confidenceScores = new ArrayList<>();

    public void init() throws Exception {
        super.init();
        Map<String, String> env = System.getenv();

        if (!env.containsKey(BenchmarkConstants.ENV_KPI_ACCURACY)) {
            throw new IllegalArgumentException("Couldn't get \"" + BenchmarkConstants.ENV_KPI_ACCURACY
                    + "\" from the environment. Aborting.");
        }
        EVAL_ACCURACY = this.model.createProperty(env.get(BenchmarkConstants.ENV_KPI_ACCURACY));

        if (!env.containsKey(BenchmarkConstants.ENV_KPI_AUC)) {
            throw new IllegalArgumentException("Couldn't get \"" + BenchmarkConstants.ENV_KPI_AUC
                    + "\" from the environment. Aborting.");
        }
        EVAL_AUC = this.model.createProperty(env.get(BenchmarkConstants.ENV_KPI_AUC));

        if (!env.containsKey(BenchmarkConstants.ENV_KPI_ROC)) {
            throw new IllegalArgumentException("Couldn't get \"" + BenchmarkConstants.ENV_KPI_ROC
                    + "\" from the environment. Aborting.");
        }
        EVAL_ROC = this.model.createProperty(env.get(BenchmarkConstants.ENV_KPI_ROC));

        if (!env.containsKey(BenchmarkConstants.ENV_KPI_EVALUATION_TIME)) {
            throw new IllegalArgumentException("Couldn't get \"" + BenchmarkConstants.ENV_KPI_EVALUATION_TIME
                    + "\" from the environment. Aborting.");
        }
        EVAL_RUN_TIME = this.model.createProperty(env.get(BenchmarkConstants.ENV_KPI_EVALUATION_TIME));

        if (!env.containsKey(BenchmarkConstants.ENV_KPI_PRECISION)) {
            throw new IllegalArgumentException("Couldn't get \"" + BenchmarkConstants.ENV_KPI_PRECISION
                    + "\" from the environment. Aborting.");
        }
        EVAL_PRECISION = this.model.createProperty(env.get(BenchmarkConstants.ENV_KPI_PRECISION));

        if (!env.containsKey(BenchmarkConstants.ENV_KPI_RECALL)) {
            throw new IllegalArgumentException("Couldn't get \"" + BenchmarkConstants.ENV_KPI_RECALL
                    + "\" from the environment. Aborting.");
        }
        EVAL_RECALL = this.model.createProperty(env.get(BenchmarkConstants.ENV_KPI_RECALL));

        if (!env.containsKey(BenchmarkConstants.ENV_FACTCHECK_THRESHOLD)) {
            throw new IllegalArgumentException("Couldn't get \"" + BenchmarkConstants.ENV_FACTCHECK_THRESHOLD
                    + "\" from the environment. Aborting.");
        }
        FACTCHECK_THRESHOLD = Double.parseDouble(env.get(BenchmarkConstants.ENV_FACTCHECK_THRESHOLD));

        logger.debug("Using FactCheck threshold value {}", FACTCHECK_THRESHOLD);
    }

    @Override
    protected void evaluateResponse(byte[] expectedData, byte[] receivedData, long taskSentTimestamp, long responseReceivedTimestamp) throws Exception {
        // getROCCurve the given response and store the result, e.g., increment internal counters
        logger.debug("evaluateResponse()");

        final String TRUE_RESPONSE = "/correct/";
        final String FALSE_RESPONSE = "/wrong/";

        //Obtain received and expected responses
        String expectedResponse = new String(expectedData);

        double receivedScore = Double.valueOf(new String(receivedData));
        String receivedResponse;

        if (receivedScore >= FACTCHECK_THRESHOLD)
            receivedResponse = TRUE_RESPONSE;
        else
            receivedResponse = FALSE_RESPONSE;

        //Increment false/true positive/negative counters
        if (expectedResponse.contains(receivedResponse)) {
            if (expectedResponse.contains(TRUE_RESPONSE))
                truePositive++;
            else
                trueNegative++;
        } else if (expectedResponse.contains(TRUE_RESPONSE) && receivedResponse.contains(FALSE_RESPONSE)) {
            falseNegative++;
        } else if (expectedResponse.contains(FALSE_RESPONSE) && receivedResponse.contains(TRUE_RESPONSE)) {
            falsePositive++;
        }

        totalRunTime += responseReceivedTimestamp - taskSentTimestamp;

        //Update accumulators for ROC/AUC calculation
        confidenceScores.add(receivedScore);
        trueLabels.add(expectedResponse.contains(TRUE_RESPONSE) ? 1 : 0);
        logger.debug("True Label {}, Confidence Score {}", expectedResponse.contains(TRUE_RESPONSE) ? 1 : 0, receivedScore);

    }

    @Override
    protected Model summarizeEvaluation() throws Exception {
        logger.debug("summarizeEvaluation()");
        // All tasks/responses have been evaluated. Summarize the results,
        // write them into a Jena model and send it to the benchmark controller.

        //Calculate AUC and obtain the points for the ROC curve
        ROCEvaluator evaluator = new ROCEvaluator(trueLabels, confidenceScores);
        ROCCurve rocCurve = evaluator.getROCCurve();

        //Calculate accuracy, precision and recall
        double accuracy = calculateAccuracy();
        double precision = calculatePrecision();
        double recall = calculateRecall();

        Resource experimentResource = model.getResource(experimentUri);
        model.add(experimentResource, RDF.type, HOBBIT.Experiment);

        //Accuracy literal
        Literal accuracyLiteral = model.createTypedLiteral(accuracy, XSDDatatype.XSDdouble);
        model.add(experimentResource, EVAL_ACCURACY, accuracyLiteral);
        logger.debug(BenchmarkConstants.ENV_KPI_ACCURACY + " added to model: " + accuracy);

        //AUC literal
        Literal aucLiteral = model.createTypedLiteral(rocCurve.calculateAUC(), XSDDatatype.XSDdouble);
        model.add(experimentResource, EVAL_AUC, aucLiteral);
        logger.debug(BenchmarkConstants.ENV_KPI_AUC + " added to model: " + rocCurve.calculateAUC());

        //AUC literal
        Literal rocLiteral = model.createTypedLiteral(rocCurve.toString(), XSDDatatype.XSDstring);
        model.add(experimentResource, EVAL_ROC, rocLiteral);
        logger.debug(BenchmarkConstants.ENV_KPI_ROC + " added to model: " + rocCurve.toString());

        //Runtime literal
        Literal timeLiteral = model.createTypedLiteral(totalRunTime, XSDDatatype.XSDlong);
        model.add(experimentResource, EVAL_RUN_TIME, timeLiteral);
        logger.debug(BenchmarkConstants.ENV_KPI_EVALUATION_TIME + " added to model: " + totalRunTime);

        //Recall literal
        Literal recallLiteral = model.createTypedLiteral(recall, XSDDatatype.XSDdouble);
        model.add(experimentResource, EVAL_RECALL, recallLiteral);
        logger.debug(BenchmarkConstants.ENV_KPI_RECALL + " added to model: " + recall);

        //Precision literal
        Literal precisionLiteral = model.createTypedLiteral(precision, XSDDatatype.XSDlong);
        model.add(experimentResource, EVAL_PRECISION, precisionLiteral);
        logger.debug(BenchmarkConstants.ENV_KPI_PRECISION + " added to model: " + precision);

        return model;
    }

    //Calculates accuracy using the necessary counters
    private double calculateAccuracy() {
        return (truePositive + trueNegative)
                / (double) (truePositive + trueNegative + falsePositive + falseNegative);
    }

    //Calculates precision using the necessary counters (relevantItemsRetrieved / retrievedItems)
    private double calculatePrecision() {
        return truePositive
                / (double) (truePositive + falsePositive);
    }

    //Calculates recall using the necessary counters (relevantItemsRetrieved / relevantItems)
    private double calculateRecall() {
        return truePositive
                / (double) (truePositive + falseNegative);
    }

    @Override
    public void close() {
        // Free the resources you requested here
        logger.debug("close()");
        // Always close the super class after yours!
        try {
            super.close();
        } catch (Exception ignored) {

        }
    }
}
