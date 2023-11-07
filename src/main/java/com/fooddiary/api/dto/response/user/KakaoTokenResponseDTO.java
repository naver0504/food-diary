package com.fooddiary.api.dto.response.user;

import lombok.Getter;
import lombok.Setter;

/**
 * https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#refresh-token
 */
@Getter
@Setter
public class KakaoTokenResponseDTO {
    private String access_token;
    private String id_token;
    private Integer expires_in;
    private String refresh_token;
    private Integer refresh_token_expires_in;
}
