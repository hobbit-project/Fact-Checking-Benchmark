package org.dice.factcheckbenchmark.benchmark.roc;

import java.util.ArrayList;
import java.util.List;

public class ROCEvaluator {

    private ArrayList<Integer> truthValues;
    private ArrayList<Double> predictedValues;

    public ROCEvaluator(ArrayList<Integer> truthLabels, ArrayList<Double> predictedScores) {

        truthValues = truthLabels;
        predictedValues = predictedScores;
    }

    public ROCCurve getROCCurve() {

        int trueStmts = (int) truthValues.stream().filter(value -> value > 0.5).count();
        int falseStmts = (int) truthValues.stream().filter(value -> value < 0.5).count();
        int trueResults = 0;
        int falseResults = 0;

        List<DoubleBooleanPair> evalStatements = new ArrayList<>(predictedValues.size());

        for (int x = 0; x < predictedValues.size(); x++) {

            // We don't want to check whether it is equal to one or zero, so let's check
            // whether it is larger than 0.5 ;-)
            evalStatements.add(new DoubleBooleanPair(predictedValues.get(x), (truthValues.get(x) > 0.5)));
        }

        ROCCurve curve = new ROCCurve(trueStmts, falseStmts);

        for (int i = 0; i < evalStatements.size(); ++i) {

            DoubleBooleanPair current = evalStatements.get(i);

            if (current.goldFlag) {
                ++trueResults;
            } else {
                ++falseResults;
            }

            // If this is the last pair OR the next pair has a different predicted value
            if ((i == (evalStatements.size() - 1)) || (evalStatements.get(i + 1).predictedValue != current.predictedValue)) {
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

    protected static class DoubleBooleanPair implements Comparable<DoubleBooleanPair> {
        private double predictedValue;
        private boolean goldFlag;

        public DoubleBooleanPair() {
        }

        public DoubleBooleanPair(double predictedValue, boolean goldFlag) {
            this.predictedValue = predictedValue;
            this.goldFlag = goldFlag;
        }

        @Override
        public int compareTo(DoubleBooleanPair o) {
            int compareResult = Double.compare(predictedValue, o.predictedValue);
            return (compareResult == 0) ? 0 : -compareResult;
        }

        @Override
        public String toString() {
            StringBuilder builder = new StringBuilder();
            builder.append('(');
            builder.append(predictedValue);
            builder.append(',');
            builder.append(goldFlag ? '1' : '0');
            builder.append(')');
            return builder.toString();
        }
    }
}