package com.pracelab.demo.dto;

import javax.validation.constraints.NotNull;

public class AdminAcceptanceDTO {
    @NotNull
    private int serviceId;
    @NotNull
    private int userId;
    @NotNull
    private boolean acceptance;
    @NotNull
    private String process;

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public boolean isAcceptance() {
        return acceptance;
    }

    public void setAcceptance(boolean acceptance) {
        this.acceptance = acceptance;
    }

    public String getProcess() {
        return process;
    }

    public void setProcess(String process) {
        this.process = process;
    }

    @Override
    public String toString() {
        return "AdminAcceptanceDTO{" +
                "serviceId=" + serviceId +
                ", userId=" + userId +
                ", acceptance=" + acceptance +
                ", process='" + process + '\'' +
                '}';
    }
}
