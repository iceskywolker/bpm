package com.pracelab.demo.dto.input;

import com.pracelab.demo.dto.Requests.CreateContract;

import java.util.Set;

public class ContractData {
    private CreateContract contractRequest;
    private String activitiUser;
    private Set<String> roles;

    public ContractData() {
    }

    public ContractData(CreateContract contractRequest, String activitiUser, Set<String> roles) {
        this.contractRequest = contractRequest;
        this.activitiUser = activitiUser;
        this.roles = roles;
    }

    public CreateContract getContractRequest() {
        return contractRequest;
    }

    public String getActivitiUser() {
        return activitiUser;
    }

    public Set<String> getRoles() {
        return roles;
    }
}
