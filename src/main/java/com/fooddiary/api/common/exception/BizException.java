package com.fooddiary.api.common.exception;

public class BizException extends RuntimeException {
    public BizException (String message) {
        super(message);
    }
}
