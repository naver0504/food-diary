package com.fooddiary.api.common.interceptor;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.context.SecurityContext;

public class preHandler {
    @Autowired
    SecurityContext securityContext;
}
