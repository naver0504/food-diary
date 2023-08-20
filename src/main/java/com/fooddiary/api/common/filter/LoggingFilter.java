package com.fooddiary.api.common.filter;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StreamUtils;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.ZoneOffset;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoggingFilter extends OncePerRequestFilter {
    private final ObjectMapper objectMapper;
    private static final String ACCESS_LOG_INDEX = "access-log";

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException {
        //MDC.put("traceId", UUID.randomUUID().toString());
        if (isAsyncDispatch(request)) {
            filterChain.doFilter(request, response);
        } else {
            doFilterWrapped(new HttpServletRequestWrapper(request), new ContentCachingResponseWrapper(response), filterChain);
        }
        //MDC.clear();
    }

    protected void doFilterWrapped(HttpServletRequestWrapper request, ContentCachingResponseWrapper response, FilterChain filterChain) throws ServletException, IOException {
        LocalDateTime startTime = null;
        LogDTO.RequestLogDTO requestLogDTO = null;
        try {
            HashMap<String, String> headerMap = new HashMap<>();
            Iterator<String> keys = request.getHeaderNames().asIterator();
            while (keys.hasNext()) {
                String name = keys.next();
                headerMap.put(name, request.getHeader(name));
            }
            requestLogDTO = new LogDTO.RequestLogDTO(headerMap, request.getRequestURI() + request.getQueryString(), request.getMethod(), request.getContentType(), request.getRemoteAddr(), request.getCookies());
            startTime = LocalDateTime.now();
            //logRequest(request, startTime);
            filterChain.doFilter(request, response);
        } finally {
            logResponse(requestLogDTO, response, startTime);
            response.copyBodyToResponse();
        }
    }

    /*
    private void logRequest(HttpServletRequestWrapper request, LocalDateTime startTime) throws IOException {
        String queryString = request.getQueryString();
        log.info("Request : {} uri=[{}] content-type=[{}]",
                request.getMethod(),
                queryString == null ? request.getRequestURI() : request.getRequestURI() + queryString,
                request.getContentType()
        );

        logPayload("Request", request.getContentType(), request.getInputStream(), startTime);
    }
     */

    private void logResponse(LogDTO.RequestLogDTO requestLogDTO, ContentCachingResponseWrapper response, LocalDateTime startTime) throws IOException {
        logPayload(requestLogDTO, response.getContentType(), response.getContentInputStream(), startTime);
    }

    private void logPayload(LogDTO.RequestLogDTO requestLogDTO, String contentType, InputStream inputStream, LocalDateTime startTime) throws IOException {
        boolean visible = isVisible(MediaType.valueOf(contentType == null ? "application/json" : contentType));
        if (visible) {
            byte[] content = StreamUtils.copyToByteArray(inputStream);
            if (content.length > 0) {
                Long timeLap = LocalDateTime.now().toEpochSecond(ZoneOffset.UTC) - startTime.toEpochSecond(ZoneOffset.UTC);
                LogDTO.ResponseLogDTO responseLogDTO = new LogDTO.ResponseLogDTO(new String(content, StandardCharsets.UTF_8), timeLap);
                String contentString = objectMapper.writeValueAsString(new LogDTO(requestLogDTO, responseLogDTO));
                log.info("{}: {}", ACCESS_LOG_INDEX, contentString);
            }
        } else {
            log.info("{}: Binary Content", ACCESS_LOG_INDEX);
        }

    }

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
}