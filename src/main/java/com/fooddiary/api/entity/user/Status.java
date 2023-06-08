package com.fooddiary.api.entity.user;

public enum Status {

    ACTIVE("active"), DELETE("delete"), SUSPENDED("suspended");

    private final String code;

    Status(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
