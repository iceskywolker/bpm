package com.pracelab.demo.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreType;

import javax.validation.constraints.NotNull;

public class OrderServiceDTO {
    @NotNull
    private int serviceId;
    @NotNull
    private int userID;
    @NotNull
    private Object config;

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getUserID() {
        return userID;
    }

    public void setUserID(int userID) {
        this.userID = userID;
    }

    public Object getConfig() {
        return config;
    }

    public void setConfig(Object config) {
        this.config = config;
    }

    @Override
    public String toString() {
        return "OrderServiceDTO{" +
                "serviceId=" + serviceId +
                ", userID=" + userID +
                ", config='" + config + '\'' +
                '}';
    }
}
