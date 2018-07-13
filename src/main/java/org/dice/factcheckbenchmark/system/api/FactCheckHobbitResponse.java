package org.dice.factcheckbenchmark.system.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.dice.factcheckbenchmark.BenchmarkConstants;

import java.io.Serializable;

//public class FactCheckHobbitResponse implements Serializable {
public class FactCheckHobbitResponse {
    private String taskId;
    private double truthValue = 0.0;
    private final String dataSet = "factbench";
    private String responseData;


    @JsonCreator
    public FactCheckHobbitResponse(@JsonProperty("taskId") String taskId,
                                   @JsonProperty("responseData") String responseData,
                                   @JsonProperty("truthValue")  double truthValue) {
        this.taskId = taskId;
        this.responseData = responseData.replaceAll("dataset/", "dataset/" + dataSet);
        this.truthValue = truthValue;
    }

    public String getTaskId() {
        return taskId;
    }

    public String getResponseData() {
        return responseData;
    }

    public double getTruthValue() {
        return truthValue;
    }
}