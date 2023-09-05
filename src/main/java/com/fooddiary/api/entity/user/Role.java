package com.fooddiary.api.entity.user;

public enum Role {
    CLIENT("client"), ADMIN("admin");
    private final String value;

    Role(String value) {this.value = value;}

    public String getValue() {return value;}
}
