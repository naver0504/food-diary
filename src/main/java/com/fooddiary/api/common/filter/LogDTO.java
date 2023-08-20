package com.fooddiary.api.common.filter;

import jakarta.servlet.http.Cookie;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;

@Getter
@AllArgsConstructor
public class LogDTO {
    private RequestLogDTO request;
    private ResponseLogDTO response;

    @Getter
    @AllArgsConstructor
    public static class RequestLogDTO {
        private HashMap<String, String> header;
        private String uri;
        private String method;
        private String contentType;
        private String remoteIp;
        private Cookie[] cookies;
    }

    @Getter
    @AllArgsConstructor
    public static class ResponseLogDTO {
        private String response;
        private Long timeLap;
    }
}
