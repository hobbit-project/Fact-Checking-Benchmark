package org.hobbit.sdk.examples.examplebenchmark.benchmark;

import org.apache.jena.datatypes.xsd.XSDDatatype;
import org.apache.jena.rdf.model.*;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.core.components.AbstractEvaluationModule;
import org.hobbit.sdk.examples.examplebenchmark.Constants;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.vocabulary.RDF;
import org.hobbit.core.components.AbstractEvaluationModule;
import org.hobbit.vocab.HOBBIT;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Map;


public class EvalModule extends AbstractEvaluationModule {
    private static final Logger logger = LoggerFactory.getLogger(EvalModule.class);

    private int truePositive = 0;
    private int falsePositive = 0;
    private int trueNegative = 0;
    private int falseNegative = 0;
    private long totalRunTime = 0;

    private Model model = ModelFactory.createDefaultModel();

    private Property EVAL_ACCURACY = null;
    private Property EVAL_RUN_TIME = null;
    private Property EVAL_ROC_AUC = null;
    private Property EVAL_PRECISION = null;
    private Property EVAL_RECALL = null;

    //Accumulators to store values to calculate ROC/AUC
    private ArrayList<Integer> trueLabels = new ArrayList<>();
    private ArrayList<Double> confidenceScores = new ArrayList<>();

    public void init() throws Exception {
        super.init();
        Map<String, String> env = System.getenv();

        if (!env.containsKey(Constants.EVALUATION_ACCURACY)) {
            throw new IllegalArgumentException("Couldn't get \"" + Constants.EVALUATION_ACCURACY
                    + "\" from the environment. Aborting.");
        }
        EVAL_ACCURACY = this.model.createProperty(env.get(Constants.EVALUATION_ACCURACY));

        if (!env.containsKey(Constants.EVALUATION_ROC_AUC)) {
            throw new IllegalArgumentException("Couldn't get \"" + Constants.EVALUATION_ROC_AUC
                    + "\" from the environment. Aborting.");
        }
        EVAL_ROC_AUC = this.model.createProperty(env.get(Constants.EVALUATION_ROC_AUC));

        if (!env.containsKey(Constants.EVALUATION_TIME)) {
            throw new IllegalArgumentException("Couldn't get \"" + Constants.EVALUATION_TIME
                    + "\" from the environment. Aborting.");
        }
        EVAL_RUN_TIME = this.model.createProperty(env.get(Constants.EVALUATION_TIME));

        if (!env.containsKey(Constants.EVALUATION_PRECISION)) {
            throw new IllegalArgumentException("Couldn't get \"" + Constants.EVALUATION_PRECISION
                    + "\" from the environment. Aborting.");
        }
        EVAL_PRECISION  = this.model.createProperty(env.get(Constants.EVALUATION_PRECISION));

        if (!env.containsKey(Constants.EVALUATION_RECALL)) {
            throw new IllegalArgumentException("Couldn't get \"" + Constants.EVALUATION_RECALL
                    + "\" from the environment. Aborting.");
        }
        EVAL_RECALL = this.model.createProperty(env.get(Constants.EVALUATION_RECALL));
    }

    @Override
    protected void evaluateResponse(byte[] expectedData, byte[] receivedData, long taskSentTimestamp, long responseReceivedTimestamp) throws Exception {
        // evaluate the given response and store the result, e.g., increment internal counters
        logger.trace("evaluateResponse()");

        //Obtain received and expected responses
        String[] receivedResponse = (new String(receivedData)).split(":\\*:");
        String expectedResponse = new String(expectedData);

        //Increment false/true positive/negative counters
        if (receivedResponse[0].contains(expectedResponse)) {
            if (expectedResponse.equals("true"))
                truePositive++;
            else
                trueNegative++;
        } else if (expectedResponse.equals("true") && receivedResponse[0].contains("false")) {
            falseNegative++;
        } else if (receivedResponse[0].contains("true") && expectedResponse.equals("false")) {
            falsePositive++;
        }

        totalRunTime += responseReceivedTimestamp-taskSentTimestamp;

        //Update accumulators for ROC/AUC calculation
        confidenceScores.add(Double.parseDouble(receivedResponse[1]));

        if (expectedResponse.equals("true")){
            trueLabels.add(1);
        }
        else{
            trueLabels.add(0);
        }
    }

    @Override
    protected Model summarizeEvaluation() throws Exception {
        logger.debug("summarizeEvaluation()");
        // All tasks/responses have been evaluated. Summarize the results,
        // write them into a Jena model and send it to the benchmark controller.

        //Calculate AUC and obtain the points for the ROC curve
        /*Curve rocCurve = new Curve.PrimitivesBuilder().predicteds(confidenceScores).actuals(trueLabels).build();
        double roc_auc = rocCurve.rocArea();
        double[][] rocPoints = rocCurve.rocPoints();*/

        //Calculate accuracy, precision and recall
        double accuracy = calculateAccuracy();
        double precision = calculatePrecision();
        double recall = calculateRecall();

        Resource experimentResource = model.getResource(experimentUri);
        model.add(experimentResource, RDF.type, HOBBIT.Experiment);

        //Accuracy literal
        Literal accuracyLiteral = model.createTypedLiteral(accuracy, XSDDatatype.XSDdouble);
        model.add(experimentResource, EVAL_ACCURACY, accuracyLiteral);
        logger.debug(Constants.EVALUATION_ACCURACY + " added to model: " + accuracy);

        //ROC/AUC literal
        /*Literal rocAucLiteral = model.createTypedLiteral(roc_auc, XSDDatatype.XSDdouble);
        model.add(experimentResource, EVAL_ROC_AUC, rocAucLiteral);
        logger.debug(Constants.EVALUATION_ROC_AUC + " added to model: " + roc_auc);*/

        //Runtime literal
        Literal timeLiteral = model.createTypedLiteral(totalRunTime, XSDDatatype.XSDlong);
        model.add(experimentResource, EVAL_RUN_TIME, timeLiteral);
        logger.debug(Constants.EVALUATION_TIME + " added to model: " + totalRunTime);

        //Recall literal
        Literal recallLiteral = model.createTypedLiteral(recall, XSDDatatype.XSDdouble);
        model.add(experimentResource, EVAL_RECALL, recallLiteral);
        logger.debug(Constants.EVALUATION_RECALL + " added to model: " + recall);

        //Precision literal
        Literal precisionLiteral = model.createTypedLiteral(precision, XSDDatatype.XSDlong);
        model.add(experimentResource, EVAL_PRECISION, precisionLiteral);
        logger.debug(Constants.EVALUATION_PRECISION + " added to model: " + precision);

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
