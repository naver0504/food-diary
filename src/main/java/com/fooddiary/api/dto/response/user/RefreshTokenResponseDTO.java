package com.fooddiary.api.dto.response.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class RefreshTokenResponseDTO {
    private String refreshToken;
    private String token;
    private Long tokenExpireAt;
    private Long refreshTokenExpireAt;
}
