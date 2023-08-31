package com.fooddiary.api.entity.user;

public enum Role {
    CLIENT("CLIENT"), ADMIN("ADMIN");
    private final String value;

    Role(String value) {this.value = value;}

    public String getValue() {return value;}
}
