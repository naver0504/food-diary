package com.fooddiary.api.dto.response.user;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserResponseDTO {
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private String token;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Status status;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private Boolean pwExpired;
    @JsonInclude(JsonInclude.Include.NON_NULL)
    private UserNewPasswordResponseDTO.Status passwordStatus;

    public enum Status {
        SUCCESS, INVALID_USER, INVALID_PASSWORD, PASSWORD_LIMIT_OVER, DUPLICATED_USER, INVALID_EMAIL
    }
}
