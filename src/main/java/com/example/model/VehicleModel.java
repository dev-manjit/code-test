package com.example.model;

import com.fasterxml.jackson.annotation.JsonProperty;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbBean;
import software.amazon.awssdk.enhanced.dynamodb.mapper.annotations.DynamoDbPartitionKey;

import java.io.Serializable;

@DynamoDbBean
public class VehicleModel implements Serializable {

    private static final long serialVersionUID = 1L;

    @JsonProperty("modelId")
    private String modelId;


    @JsonProperty("count")
    private Integer count;

    public VehicleModel() {
    }

    public VehicleModel(String modelId) {
        this.modelId = modelId;
    }

    public VehicleModel(String modelId, Integer count) {
        this.modelId = modelId;
        this.count =count;
    }


    @DynamoDbPartitionKey
    public String getModelId() {
        return modelId;
    }

    public void setModelId(String modelId) {
        this.modelId = modelId;
    }

    public Integer getCount() {
        return count;
    }

    public void setCount(Integer count) {
        this.count = count;
    }

    @Override
    public String toString() {
        return "VehicleModel{" +
                "modelId='" + modelId + '\'' +
                ", count=" + count +
                '}';
    }
}
