package org.dice.factcheckbenchmark.system.api;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonProperty;

import java.io.Serializable;

//public class FactCheckHobbitResponse implements Serializable {
public class FactCheckHobbitResponse {
    private String taskId;
    private double truthValue = 0.0;
    private String fileTrace;

    private String responseData;


    @JsonCreator
    public FactCheckHobbitResponse(@JsonProperty("taskId") String taskId,
                                   @JsonProperty("responseData") String responseData,
                                   @JsonProperty("truthValue")  double truthValue,
                                   @JsonProperty("fileTrace") String fileTrace) {
        this.taskId = taskId;
        this.responseData = responseData;
        this.truthValue = truthValue;
        this.fileTrace = fileTrace;
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

    public String getFileTrace() {
        return fileTrace;
    }
}