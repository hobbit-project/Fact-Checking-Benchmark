package org.dice.factcheckbenchmark.benchmark.roc;

public class F1OptimizedResult {

    protected double f1score = 0;
    protected double precision = 0;
    protected double recall = 0;
    protected double confidence = 0;
    
    public F1OptimizedResult() {
    }
    
    
    public F1OptimizedResult(double f1score, double precision, double recall, double confidence) {
        this.f1score = f1score;
        this.precision = precision;
        this.recall = recall;
        this.confidence = confidence;
    }


    public void updateIfBetter(double f1score, double precision, double recall, double confidence) {
        if(f1score > this.f1score) {
            this.f1score = f1score;
            this.precision = precision;
            this.recall = recall;
            this.confidence = confidence;
        }
    }

    /**
     * @return the f1score
     */
    public double getF1score() {
        return f1score;
    }

    /**
     * @param f1score the f1score to set
     */
    public void setF1score(double f1score) {
        this.f1score = f1score;
    }

    /**
     * @return the precision
     */
    public double getPrecision() {
        return precision;
    }

    /**
     * @param precision the precision to set
     */
    public void setPrecision(double precision) {
        this.precision = precision;
    }

    /**
     * @return the recall
     */
    public double getRecall() {
        return recall;
    }

    /**
     * @param recall the recall to set
     */
    public void setRecall(double recall) {
        this.recall = recall;
    }

    /**
     * @return the confidence
     */
    public double getConfidence() {
        return confidence;
    }

    /**
     * @param confidence the confidence to set
     */
    public void setConfidence(double confidence) {
        this.confidence = confidence;
    }
}
