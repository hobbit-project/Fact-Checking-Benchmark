package org.dice.factcheckbenchmark.benchmark.roc;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ROCEvaluator {

    /*
     * 
     * ROC/AUC implementation extracted from:
     * https://github.com/dice-group/gerbil.git
     */
    private List<DoubleBooleanPair> evalStatements = new ArrayList<>();

    private int trueStmts = 0;
    private int falseStmts = 0;

    public ROCEvaluator(ArrayList<Boolean> truthLabels, ArrayList<Double> predictedScores) {
        trueStmts = (int) truthLabels.stream().filter(value -> value).count();
        falseStmts = (int) truthLabels.stream().filter(value -> !value).count();

        for (int x = 0; x < predictedScores.size(); x++) {
            evalStatements.add(new DoubleBooleanPair(predictedScores.get(x), truthLabels.get(x)));
        }
        Collections.sort(evalStatements);
    }

    public ROCCurve getROCCurve() {

        int trueResults = 0;
        int falseResults = 0;

        ROCCurve curve = new ROCCurve(trueStmts, falseStmts);

        for (int i = 0; i < evalStatements.size(); ++i) {

            DoubleBooleanPair current = evalStatements.get(i);

            if (current.goldFlag) {
                ++trueResults;
            } else {
                ++falseResults;
            }

            // If this is the last pair OR the next pair has a different predicted value
            if ((i == (evalStatements.size() - 1))
                    || (evalStatements.get(i + 1).predictedValue != current.predictedValue)) {
                // check if there are only steps up
                if (falseResults == 0) {
                    for (int j = 0; j < trueResults; ++j) {
                        curve.addUp();
                    }
                } else if (trueResults == 0) {
                    for (int j = 0; j < falseResults; ++j) {
                        curve.addRight();
                    }
                } else {
                    curve.addDiagonally(trueResults, falseResults);
                }
                trueResults = 0;
                falseResults = 0;
            }
        }
        curve.finishCurve();
        return curve;
    }

    public F1OptimizedResult calculateOptimizedF1Score() {

        // Filter all facts that where not answered by the system
        int missingTrueFacts = 0;
        int missingFalseFacts = 0;
        List<DoubleBooleanPair> filtered = new ArrayList<>(evalStatements.size());
        for (DoubleBooleanPair current : evalStatements) {
            if (current.predictedValue != null) {
                filtered.add(current);
            } else {
                if (current.goldFlag) {
                    ++missingTrueFacts;
                } else {
                    ++missingFalseFacts;
                }
            }
        }

        int tp_true = 0;
        int tp_false = falseStmts - missingFalseFacts;
        int fp_true = 0;
        int fp_false = trueStmts - missingTrueFacts;
        int fn_true = trueStmts;
        int fn_false = missingFalseFacts;
//        int tp = falseStmts - missingFalseFacts;
//        int fp = 0;
//        int fn = trueStmts + missingFalseFacts;
        // int remainingTrue = trueStmts - missingTrueFacts;
        // int remainingFalse = falseStmts - missingFalseFacts;
        double precision, recall, f1;

        precision = (tp_true + tp_false) / (double) (tp_true + tp_false + fp_true + fp_false);
        if (Double.isNaN(precision)) {
            precision = 0;
        }
        recall = (tp_true + tp_false) / (double) (tp_true + tp_false + fn_true + fn_false);
        if (Double.isNaN(recall)) {
            recall = 0;
        }
        if ((precision <= 0.0) || (recall <= 0.0)) {
            f1 = 0;
        } else {
            f1 = (2 * precision * recall) / (precision + recall);
        }
        F1OptimizedResult result = new F1OptimizedResult(f1, precision, recall, Double.POSITIVE_INFINITY);

        for (DoubleBooleanPair current : filtered) {
            if (current.goldFlag) {
                ++tp_true;
                --fn_true;
                --fp_false;
            } else {
                --tp_false;
                ++fn_false;
                ++fp_true;
            }
            precision = (tp_true + tp_false) / (double) (tp_true + tp_false + fp_true + fp_false);
            if (Double.isNaN(precision)) {
                precision = 0;
            }
            recall = (tp_true + tp_false) / (double) (tp_true + tp_false + fn_true + fn_false);
            if (Double.isNaN(recall)) {
                recall = 0;
            }
            if ((precision <= 0.0) || (recall <= 0.0)) {
                f1 = 0;
            } else {
                f1 = (2 * precision * recall) / (precision + recall);
            }
            result.updateIfBetter(f1, precision, recall, current.predictedValue);
        }
        return result;
    }

    protected static class DoubleBooleanPair implements Comparable<DoubleBooleanPair> {
        private Double predictedValue;
        private boolean goldFlag;

        public DoubleBooleanPair() {

        }

        public DoubleBooleanPair(double predictedValue, boolean goldFlag) {
            this.predictedValue = predictedValue;
            this.goldFlag = goldFlag;
        }

        @Override
        public int compareTo(DoubleBooleanPair o) {
            // Handle null values by moving them to the end of the collection
            if (predictedValue == null) {
                if (o.predictedValue == null) {
                    return 0;
                } else {
                    return 1;
                }
            }
            if (o.predictedValue == null) {
                return -1;
            }
            // both values are not null. Compare the double values as usual
            return -1*predictedValue.compareTo(o.predictedValue);
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append('(');
            builder.append(predictedValue);
            builder.append(',');
            builder.append(goldFlag);
            builder.append(')');
            return builder.toString();
        }
    }
}