package com.fooddiary.api.common.filter;

import static com.fooddiary.api.common.constants.UserConstants.MAIL_NAME;
import static com.fooddiary.api.common.constants.UserConstants.TOKEN_NAME;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;

import javax.annotation.Nullable;

import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.UserService;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {
    private static final String ACCESS_LOG_INDEX = "access-log";
    private static final int TRANSFER_BODY_SIZE = 1000;
    private static final String ELB_HEALTH_CHECKER_HEADER_NAME_KEYWORD = "HealthChecker";
    private final ObjectMapper objectMapper;
    private final UserService userService;

    private static boolean isVisible(MediaType mediaType) {
        final List<MediaType> VISIBLE_TYPES = Arrays.asList(
                MediaType.valueOf("text/*"),
                MediaType.APPLICATION_FORM_URLENCODED,
                MediaType.APPLICATION_JSON,
                MediaType.APPLICATION_XML,
                MediaType.valueOf("application/*+json"),
                MediaType.valueOf("application/*+xml"),
                MediaType.MULTIPART_FORM_DATA
        );

        return VISIBLE_TYPES.stream()
                            .anyMatch(visibleType -> visibleType.includes(mediaType));
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        //MDC.put("traceId", UUID.randomUUID().toString());
        if ((request.getHeader("user-agent") != null &&
             request.getHeader("user-agent").contains(ELB_HEALTH_CHECKER_HEADER_NAME_KEYWORD))
            || isAsyncDispatch(request)) {
            filterChain.doFilter(request, response);
        } else {
            doFilterWrapped(new RequestWrapper(request), new ContentCachingResponseWrapper(response),
                            filterChain);
        }
        //MDC.clear();
    }

    protected void doFilterWrapped(HttpServletRequestWrapper request, ContentCachingResponseWrapper response,
                                   FilterChain filterChain) throws ServletException, IOException {
        final LocalDateTime startTime = LocalDateTime.now();
        LogDTO.RequestLogDTO requestLogDTO = null;
        try {
            final HashMap<String, String> headerMap = new HashMap<>();
            final Iterator<String> keys = request.getHeaderNames().asIterator();
            while (keys.hasNext()) {
                final String name = keys.next();
                headerMap.put(name, request.getHeader(name));
            }
            final String uri = request.getQueryString() != null ?
                               request.getRequestURI() + '?' + request.getQueryString() :
                               request.getRequestURI();
            final byte[] body = StreamUtils.copyToByteArray(request.getInputStream());
            requestLogDTO = new LogDTO.RequestLogDTO(startTime, headerMap, uri, request.getMethod(),
                                                     request.getContentType(), body, request.getRemoteAddr(),
                                                     request.getCookies());
            filterChain.doFilter(request, response);
        } finally {
            final User user = userService.getValidUser(request.getHeader(MAIL_NAME),
                                                       request.getHeader(TOKEN_NAME));
            LogDTO.UserDTO userDTO = null;
            if (user != null) {
                userDTO = new LogDTO.UserDTO(user.getEmail(), user.getName());
                logPayload(requestLogDTO, userDTO, response, startTime);
            } else {
                logPayload(requestLogDTO, userDTO, response, startTime);
            }
            response.copyBodyToResponse();
        }
    }

    private void logPayload(@Nullable LogDTO.RequestLogDTO requestLogDTO, @Nullable LogDTO.UserDTO userDTO,
                            ContentCachingResponseWrapper response, LocalDateTime startTime)
            throws IOException {
        final String contentType = response.getContentType();
        final InputStream inputStream = response.getContentInputStream();
        final int statusCode = response.getStatus();
        final boolean visible = isVisible(
                MediaType.valueOf(contentType == null ? "application/json" : contentType));
        String content = null;
        if (visible) {
            content = new String(StreamUtils.copyToByteArray(inputStream), StandardCharsets.UTF_8);
            if (content.length() > TRANSFER_BODY_SIZE) {
                content = content.substring(0, TRANSFER_BODY_SIZE) + "...";
            }
        }
        final Long timeLap = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - startTime.toEpochSecond(
                ZoneOffset.UTC);
        final LogDTO.ResponseLogDTO responseLogDTO = new LogDTO.ResponseLogDTO(statusCode, content, timeLap);

        final String contentString = objectMapper.writeValueAsString(
                new LogDTO(requestLogDTO, responseLogDTO, userDTO));
        log.info("{}: {}", ACCESS_LOG_INDEX, contentString);
    }
}