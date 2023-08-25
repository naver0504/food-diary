package com.fooddiary.api.dto.response;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewPwResponseDTO {

    private Status status;
    public enum Status {
        SUCCESS, EMPTY_PASSWORD, SHORT_PASSWORD, NOT_ALPHABETIC_PASSWORD, INCLUDE_DIGIT_CHARACTER, INCLUDE_SYMBOLIC_CHARACTER
    }
}
