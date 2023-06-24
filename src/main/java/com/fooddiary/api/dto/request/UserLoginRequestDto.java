package com.fooddiary.api.dto.request;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserLoginRequestDto {
    private String email;
    private String password;
}
