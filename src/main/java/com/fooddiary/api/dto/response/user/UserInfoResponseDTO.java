package com.fooddiary.api.dto.response.user;

import com.fooddiary.api.entity.user.Role;
import com.fooddiary.api.entity.user.Status;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class UserInfoResponseDTO {
    private Role role;
    private Status status;
}
