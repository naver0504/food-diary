package com.fooddiary.api.common.external;

import com.fooddiary.api.common.interceptor.RestClientInterceptor;
import lombok.AllArgsConstructor;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

/**
 * 외부 api의 호출을 관리하는 곳입니다.
 */
@Component
@AllArgsConstructor
public class ExternalHttpApis {

    private final RestClientInterceptor restClientInterceptor;
    private static final SimpleClientHttpRequestFactory simpleClientHttpRequestFactory;

    static {
        simpleClientHttpRequestFactory = new SimpleClientHttpRequestFactory();
        simpleClientHttpRequestFactory.setConnectTimeout(5 * 000);
        simpleClientHttpRequestFactory.setReadTimeout(10 * 000);
    }

    public RestClient getGoogleAuthClient() {

        return RestClient.builder()
                // .messageConverters(converters -> converters.add(new FormHttpMessageConverter()))
                .requestFactory(new BufferingClientHttpRequestFactory(simpleClientHttpRequestFactory))
                .baseUrl("https://oauth2.googleapis.com")
                .requestInterceptor(restClientInterceptor)
                .build();
    }

    public RestClient getGoogleAccountsClient() {
        return RestClient.builder()
                .requestFactory(new BufferingClientHttpRequestFactory(simpleClientHttpRequestFactory))
                .baseUrl("https://accounts.google.com")
                .requestInterceptor(restClientInterceptor)
                .build();
    }

    public RestClient getKakaoApiClient() {
        return RestClient.builder()
                .requestFactory(new BufferingClientHttpRequestFactory(simpleClientHttpRequestFactory))
                .baseUrl("https://kapi.kakao.com")
                .requestInterceptor(restClientInterceptor)
                .build();
    }

    public RestClient getKakaoAuthClient() {
        return RestClient.builder()
                .requestFactory(new BufferingClientHttpRequestFactory(simpleClientHttpRequestFactory))
                .baseUrl("https://kauth.kakao.com")
                .requestInterceptor(restClientInterceptor)
                .build();
    }

}
