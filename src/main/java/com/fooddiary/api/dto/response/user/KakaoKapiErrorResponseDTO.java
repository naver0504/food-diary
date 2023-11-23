package com.fooddiary.api.dto.response.user;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class KakaoKapiErrorResponseDTO {
    private String msg;
    private int code;
}
