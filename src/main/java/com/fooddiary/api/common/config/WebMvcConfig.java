package com.fooddiary.api.common.config;

import com.fooddiary.api.common.interceptor.Interceptor;
import com.fooddiary.api.dto.request.diary.DiaryTimeConverter;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.format.FormatterRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebMvcConfig implements WebMvcConfigurer {

    private final Interceptor interceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(interceptor)
                .excludePathPatterns("/css/**", "/images/**", "/js/**", "/assets/**", "/favicon.ico", "/*.html");
    }

    /*
    @Override
    public void extendHandlerExceptionResolvers(List<HandlerExceptionResolver> resolvers) {
        // exceptionHandler 가 이미 component 로 등록되어서 중복 이벤트 발생됩니다.
       // resolvers.add(exceptionHandler);
    }
*/

    @Override
    public void addFormatters(FormatterRegistry registry) {
        registry.addConverter(new DiaryTimeConverter());
    }

}