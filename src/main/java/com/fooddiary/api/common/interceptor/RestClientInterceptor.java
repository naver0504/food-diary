package com.fooddiary.api.common.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpRequest;
import org.springframework.http.client.ClientHttpRequestExecution;
import org.springframework.http.client.ClientHttpRequestInterceptor;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.stereotype.Component;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Slf4j
@Component
public class RestClientInterceptor implements ClientHttpRequestInterceptor {
    @Override
    public ClientHttpResponse intercept(HttpRequest request, byte[] body, ClientHttpRequestExecution execution) throws IOException {
        String uuid = UUID.randomUUID().toString();
        log.info("request;;" + " uuid;" + uuid + ' ' + request.getMethod().toString() + " " + request.getURI().toString() + "\nheader;" + request.getHeaders().toString() + "\nbody;" + new String(body));
        try {
            ClientHttpResponse response = execution.execute(request, body);
            int maxChar = 0;
            InputStream inputStream = response.getBody();
            StringBuilder textBuilder = new StringBuilder();
            try (Reader reader = new BufferedReader(new InputStreamReader
                    (inputStream, StandardCharsets.UTF_8))) {
                int c = 0;
                while ((c = reader.read()) != -1) {
                    textBuilder.append((char) c);
                    maxChar++;
                    if (maxChar >= 2000) { // 응답본문은 약 2kb까지만 저장하기로 하자
                        textBuilder.append('.').append('.').append('.');
                        break;
                    }
                }
            }
            log.info("response;;" + " uuid;" + uuid + ' ' + response.getStatusCode().toString() + "\nheader;" + response.getHeaders().toString() + "\nbody;" + textBuilder.toString());
            return response;
        } catch(Exception e) {
            log.info("response;;" + " uuid;" + uuid + " was failed because of IOException");
            throw new IOException(e);
        }
    }
}
