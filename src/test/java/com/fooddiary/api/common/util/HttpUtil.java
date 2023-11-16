package com.fooddiary.api.common.util;

import org.springframework.http.HttpHeaders;

public final class HttpUtil {
    public static HttpHeaders makeHeader() {
        final HttpHeaders httpHeaders = new HttpHeaders();
        httpHeaders.add("login-from", "none");
        httpHeaders.add("token", "asdf");
        httpHeaders.add("refresh-token", "refreshToken");
        return httpHeaders;
    }
}
