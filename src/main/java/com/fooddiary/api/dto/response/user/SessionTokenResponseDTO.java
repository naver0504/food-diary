package com.fooddiary.api.dto.response.user;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class SessionTokenResponseDTO {
    private String accessToken;
    private String refreshToken;
    private Long accessTokenExpireAt;
}
