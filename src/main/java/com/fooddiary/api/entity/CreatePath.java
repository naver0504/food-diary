package com.fooddiary.api.entity;

public enum CreatePath {

    GOOGLE("GOOGLE"),KAKAO("KAKAO"),NONE("NONE");

    private String code;

    private CreatePath(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
