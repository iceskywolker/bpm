package com.pracelab.demo.dto;

import javax.validation.constraints.NotNull;

public class ProcessInfoDTO {
    @NotNull
    private  int serviceId;
    @NotNull
    private int userID;


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
}
