package com.fooddiary.api.common.interceptor;

import java.io.Serial;
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

import com.fooddiary.api.common.exception.LoginException;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

import static com.fooddiary.api.common.constants.UserConstants.*;

@Component
@RequiredArgsConstructor
public class Interceptor implements HandlerInterceptor {

    private final UserService userService;
    private final Set<String> bypassUri = new HashSet<>() {
        @Serial
        private static final long serialVersionUID = 924643924179276764L;

        {
            add("/");
            // add("/index.html"); // 두 path /, /index.html 는 상태확인용으로 넣었습니다.
            // add("/google879544d728805282.html"); // google로그인 인증을 위해 google에서 도메인의 소유권 확인하려고 종종 호출하는 페이지
            add("/user/new");
            add("/user/login");
            add("/user/is-login");
            add("/user/reset-password");
            add("/app/version"); //앱 버전 확인용
            add("/user/google-login-callback");
            add("/user/session");
            add("/user/refresh-token");
        }
    };

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler)
            throws Exception {

        if (bypassUri.contains(request.getRequestURI())) {return true;}

        final User user;
        try {
            user = userService.getValidUser(request.getHeader(LOGIN_FROM_KEY), request.getHeader(TOKEN_KEY), request.getHeader(REQUEST_AGENT_KEY));
        } catch (IllegalArgumentException e) {
            throw new LoginException(LOGIN_REQUEST_KEY);
        }
        if (user == null) {
            throw new LoginException(LOGIN_REQUEST_KEY);
        }

        final ArrayList<SimpleGrantedAuthority> simpleGrantedAuthority = new ArrayList<>();
        simpleGrantedAuthority.add(new SimpleGrantedAuthority("all"));
        final RememberMeAuthenticationToken userDataAuthenticationTokenByEmail =
                new RememberMeAuthenticationToken(
                        user.getEmail(), user, simpleGrantedAuthority);
        SecurityContextHolder.getContext().setAuthentication(userDataAuthenticationTokenByEmail);

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
