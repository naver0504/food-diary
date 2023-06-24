package com.fooddiary.api.common.config;

import java.util.List;

import com.fooddiary.api.common.interceptor.Interceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final Interceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .excludePathPatterns("/css/**", "/images/**", "/js/**");
    }

    /*
    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        // exceptionHandler 가 이미 component 로 등록되어서 중복 이벤트 발생됩니다.
       // resolvers.add(exceptionHandler);
    }
*/
}