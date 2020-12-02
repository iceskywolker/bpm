package com.pracelab.demo.dto;

public class ActionResponseDTO {
    private String status;
    private String processId;
    private String taskId;

    public ActionResponseDTO() {}

    public ActionResponseDTO(String status, String processId, String taskId) {
        this.status = status;
        this.processId = processId;
        this.taskId = taskId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getProcessId() {
        return processId;
    }

    public void setProcessId(String processId) {
        this.processId = processId;
    }

    public String getTaskId() {
        return taskId;
    }

    public void setTaskId(String taskId) {
        this.taskId = taskId;
    }
}
