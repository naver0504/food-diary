package com.fooddiary.api.dto.response.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNewPasswordResponseDTO {

    private Status status;
    public enum Status {
        SUCCESS, INVALID_PASSWORD, EMPTY_PASSWORD, SHORT_PASSWORD, NOT_ALPHABETIC_PASSWORD, INCLUDE_DIGIT_CHARACTER, INCLUDE_SYMBOLIC_CHARACTER
    }
}
