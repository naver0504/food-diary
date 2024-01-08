package com.fooddiary.api.common.exception;

public class LoginException extends RuntimeException {
    public LoginException(String message) {
        super(message);
    }
}
