package com.fooddiary.api.dto.response;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserResponseDto {
    private String token;
    private Status status;

    public enum Status {
        SUCCESS, INVALID_USER, INVALID_PASSWORD
    }
}
