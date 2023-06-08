package com.fooddiary.api.entity.user;

public enum CreatePath {

    GOOGLE("google"), KAKAO("kakao"), NONE("none");

    private final String code;

    CreatePath(String code) {
        this.code = code;
    }

    public String getCode() {
        return code;
    }
}
