package com.pracelab.demo.dto;

import javax.validation.constraints.NotNull;

public class Contract {
    @NotNull
    private String processName;

    @NotNull
    private String state;

    private UserStep actionRequired;

    public Contract(@NotNull String processName, @NotNull String state, UserStep actionRequired) {
        this.processName = processName;
        this.state = state;
        this.actionRequired = actionRequired;
    }

    public String getProcessName() {
        return processName;
    }

    public String getState() {
        return state;
    }

    public UserStep getActionRequired() {
        return actionRequired;
    }

    @Override
    public String toString() {
        return "Contract{" +
                "processName='" + processName + '\'' +
                ", state='" + state + '\'' +
                ", actionRequired=" + actionRequired +
                '}';
    }
}
