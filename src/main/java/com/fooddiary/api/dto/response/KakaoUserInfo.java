package com.fooddiary.api.dto.response;

import java.time.LocalDateTime;

import org.joda.time.DateTime;

import lombok.Getter;
import lombok.Setter;

/**
 * https://developers.kakao.com/docs/latest/ko/kakaologin/rest-api#req-user-info
 * 여기서는 로그인과 앱 탈퇴에 필요한 최소 정보만을 구성합니다.
 */
@Getter
@Setter
public class KakaoUserInfo {
    private Long id;
    private Boolean has_signed_up;
    private String connected_at;
    private KakaoAccount kakao_account;

    @Getter
    @Setter
    public static class KakaoAccount {
        private Boolean is_email_valid;
        private Boolean is_email_verified;
        private String email;
        private String name;
    }
}
