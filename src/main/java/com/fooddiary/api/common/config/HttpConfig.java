package com.fooddiary.api.common.config;

import org.apache.http.impl.conn.PoolingHttpClientConnectionManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class HttpConfig {
    @Bean
    public PoolingHttpClientConnectionManager poolingHttpClientConnectionManager() {
        PoolingHttpClientConnectionManager poolingHttpClientConnectionManager = new PoolingHttpClientConnectionManager();
        // 허용되는 최대 커넥션 수
        poolingHttpClientConnectionManager.setMaxTotal(200);
        // setMaxPerRoute는 경로를 미리 알고 있는 경우 사용
        // setMaxPerRoute에 의해 경로가 지정되지 않은 호출에 대해 connection 갯수를 설정
        // 라우팅할 경로에 대한 커넥션
        poolingHttpClientConnectionManager.setDefaultMaxPerRoute(20);

        return poolingHttpClientConnectionManager;
    }
}
