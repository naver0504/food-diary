package com.fooddiary.api.common.interceptor;

import com.fooddiary.api.FileStorageService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.Nullable;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import java.util.ArrayList;

@Component
@RequiredArgsConstructor
public class Interceptor implements HandlerInterceptor {

    private final FileStorageService fileStorageService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        String key = "test";
        String principal = "user";
        ArrayList<SimpleGrantedAuthority> simpleGrantedAuthority = new ArrayList<>();
        simpleGrantedAuthority.add(new SimpleGrantedAuthority("all"));
        RememberMeAuthenticationToken usernamePasswordAuthenticationToken = new RememberMeAuthenticationToken(key, principal, simpleGrantedAuthority);
        SecurityContextHolder.getContext().setAuthentication(usernamePasswordAuthenticationToken);
        return true;
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler,
                           @Nullable ModelAndView modelAndView) throws Exception {
    }
    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler,
                         @Nullable Exception ex) throws Exception {
    }
}
