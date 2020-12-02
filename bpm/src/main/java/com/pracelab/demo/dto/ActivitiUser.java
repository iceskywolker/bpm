package com.pracelab.demo.dto;

import org.keycloak.adapters.springsecurity.account.SimpleKeycloakAccount;

import java.util.Set;

public class ActivitiUser {
    private SimpleKeycloakAccount kAccount;

    public ActivitiUser(SimpleKeycloakAccount kAccount) {
        this.kAccount = kAccount;
    }

    public String getPrefferedUserName() {
        return this.kAccount.getKeycloakSecurityContext().getToken().getPreferredUsername();
    }

    public Set<String> getRoles() {
        return this.kAccount.getRoles();
    }
}
