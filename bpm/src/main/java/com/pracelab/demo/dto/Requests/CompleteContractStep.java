package com.pracelab.demo.dto.Requests;

import javax.validation.constraints.NotNull;

public class CompleteContractStep {
    @NotNull
    private String stepId;
    @NotNull
    private String formData;

    public String getStepId() {
        return stepId;
    }

    public void setStepId(String stepId) {
        this.stepId = stepId;
    }

    public String getFormData() {
        return formData;
    }

    public void setFormData(String formData) {
        this.formData = formData;
    }

    @Override
    public String toString() {
        return "CompleteContractStep{" +
                "stepId='" + stepId + '\'' +
                ", formData='" + formData + '\'' +
                '}';
    }
}
