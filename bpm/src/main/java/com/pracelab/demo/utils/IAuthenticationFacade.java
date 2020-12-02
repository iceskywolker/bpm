package com.pracelab.demo.utils;

import org.springframework.security.core.Authentication;

import java.security.Principal;


public interface IAuthenticationFacade {
    Authentication getAuthentication();
}

