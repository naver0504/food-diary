package com.fooddiary.api.dto.request.user;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class UserLoginRequestDTO {
    private String email;
    private String password;
}