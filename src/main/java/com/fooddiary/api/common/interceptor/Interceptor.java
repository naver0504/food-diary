package com.fooddiary.api.common.interceptor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;

import org.springframework.lang.Nullable;
import org.springframework.security.authentication.RememberMeAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import com.fooddiary.api.FileStorageService;
import com.fooddiary.api.service.UserService;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Component
@RequiredArgsConstructor
public class Interceptor implements HandlerInterceptor {

    private final FileStorageService fileStorageService;
    private final UserService userService;
    private final Set<String> bypassUri = new HashSet<>() {{
        add("/user/new");
    }};

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (bypassUri.contains(request.getRequestURI())) {return true;}

        Cookie[] cookies = request.getCookies();
        // userService.isValid();

        String key = "test";
        String principal = "user";
        ArrayList<SimpleGrantedAuthority> simpleGrantedAuthority = new ArrayList<>();
        simpleGrantedAuthority.add(new SimpleGrantedAuthority("all"));
        RememberMeAuthenticationToken usernamePasswordAuthenticationToken = new RememberMeAuthenticationToken(
                key, principal, simpleGrantedAuthority);
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
