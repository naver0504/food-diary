package com.fooddiary.api.dto.response.user;

import lombok.Getter;
import lombok.Setter;

/**
 * https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#get-token-info
 */
@Getter
@Setter
public class KakaoTokenInfoResponseDTO {
    private Long id;
    private Integer expires_in;
    private Integer app_id;
}
