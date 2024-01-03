package com.fooddiary.api.dto.response.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class GoogleTokenInfoResponseDTO {
    private String email;
    private boolean email_verified;
}
