package com.pracelab.demo.dto.Requests;

import com.fasterxml.jackson.annotation.JsonProperty;

import javax.validation.constraints.NotNull;

public class CreateContract {
    @NotNull
    private int serviceId;
    @NotNull
    @JsonProperty("space_id")
    private String spaceId;
    @NotNull
    @JsonProperty("contract_type")
    private String contractType;

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getSpaceId() {
        return spaceId;
    }

    public void setSpaceId(String spaceId) {
        this.spaceId = spaceId;
    }

    public String getContractType() {
        return contractType;
    }

    public void setContractType(String contractType) {
        this.contractType = contractType;
    }

    @Override
    public String toString() {
        return "CreateContract{" +
                "serviceId=" + serviceId +
                ", spaceId='" + spaceId + '\'' +
                ", contractType='" + contractType + '\'' +
                '}';
    }
}
