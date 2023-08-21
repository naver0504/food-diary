package com.fooddiary.api.common.filter;

import jakarta.servlet.http.Cookie;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.HashMap;

@Getter
@AllArgsConstructor
public class LogDTO {
    private RequestLogDTO request;
    private ResponseLogDTO response;
    private UserDTO user;

    @Getter
    @AllArgsConstructor
    public static class RequestLogDTO {
        private LocalDateTime startTime;
        private HashMap<String, String> header;
        private String uri;
        private String method;
        private String contentType;
        private byte[] body;
        private String remoteIp;
        private Cookie[] cookies;
    }

    @Getter
    @AllArgsConstructor
    public static class ResponseLogDTO {
        private int statusCode;
        private String response;
        private Long elapsedSeconds;
    }

    @Getter
    @AllArgsConstructor
    public static class UserDTO {
        private String email;
        private String name;
    }
}
