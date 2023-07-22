package com.fooddiary.api.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserResponseDto {
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private String token;
    private Status status;
    @JsonInclude(value = JsonInclude.Include.NON_NULL)
    private Boolean pwExpired;

    public enum Status {
        SUCCESS, INVALID_USER, INVALID_PASSWORD
    }
}
