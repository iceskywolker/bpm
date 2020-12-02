package com.pracelab.demo.dto;

public class TaskDTO {
    private String processId;
    private String taskId;
    // Nazwa user tasku okreslona w modelu
    private String name;
    // Nazwa identyfikatora zadania okreslona w modelu
    private String taskDefinitionKey;
    private String status;

    private OrderServiceDTO data;

    public TaskDTO(String processId, String taskId, String name, String taskDefinitionKey, String status, OrderServiceDTO data) {
        this.processId = processId;
        this.taskId = taskId;
        this.name = name;
        this.taskDefinitionKey = taskDefinitionKey;
        this.status = status;
        this.data = data;
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

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getTaskDefinitionKey() {
        return taskDefinitionKey;
    }

    public void setTaskDefinitionKey(String taskDefinitionKey) {
        this.taskDefinitionKey = taskDefinitionKey;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public OrderServiceDTO getData() {
        return data;
    }
}
