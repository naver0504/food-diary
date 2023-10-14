package com.fooddiary.api.dto.request.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserNewPasswordRequestDTO {
    private String password;
    private String newPassword;
}
