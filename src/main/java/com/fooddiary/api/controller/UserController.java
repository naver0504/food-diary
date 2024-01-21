package com.fooddiary.api.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;

import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fooddiary.api.common.constants.UserConstants;
import com.fooddiary.api.common.exception.LoginException;
import com.fooddiary.api.dto.request.user.UserLoginRequestDTO;
import com.fooddiary.api.dto.request.user.UserNewPasswordRequestDTO;
import com.fooddiary.api.dto.request.user.UserNewRequestDTO;
import com.fooddiary.api.dto.request.user.UserResetPasswordRequestDTO;
import com.fooddiary.api.dto.response.diary.DiaryStatisticsQueryDslResponseDTO;
import com.fooddiary.api.dto.response.user.RefreshTokenResponseDTO;
import com.fooddiary.api.dto.response.user.UserInfoResponseDTO;
import com.fooddiary.api.dto.response.user.UserNewPasswordResponseDTO;
import com.fooddiary.api.dto.response.user.UserResponseDTO;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.user.UserResignService;
import com.fooddiary.api.service.user.UserService;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;
    private final UserResignService userResignService;

    @GetMapping("/info")
    public ResponseEntity<UserInfoResponseDTO> userInfo(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getUserInfo(user));
    }

    @GetMapping("/is-login")
    public ResponseEntity<HttpStatus> isLogin(HttpServletRequest request) throws GeneralSecurityException, IOException, InterruptedException {
        User user;
        try {
            user = userService.getValidUser(request.getHeader(UserConstants.LOGIN_FROM_KEY), request.getHeader(UserConstants.TOKEN_KEY), request.getHeader(UserConstants.REQUEST_AGENT_KEY));
        } catch (IllegalArgumentException e) { // 잘못된 구글 토큰값이 들어올때
            throw new LoginException(UserConstants.LOGIN_REQUEST_KEY);
        }
        if (user == null) {
            throw new LoginException(UserConstants.LOGIN_REQUEST_KEY);
        }
        return ResponseEntity.ok().build();
    }

    @PostMapping("/new")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody
                                                      UserNewRequestDTO userDto) {
        return ResponseEntity.ok(userService.createUser(userDto));
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> loginUser(@RequestBody
                                                     UserLoginRequestDTO userDto) {
        return ResponseEntity.ok(userService.loginUser(userDto));
    }

    @PostMapping("/reset-password")
    public ResponseEntity<UserResponseDTO> resetPw(@RequestBody UserResetPasswordRequestDTO userResetPasswordRequestDTO) {
        return ResponseEntity.ok(userService.resetPw(userResetPasswordRequestDTO.getEmail()));
    }

    @PostMapping("/new-password")
    public ResponseEntity<UserNewPasswordResponseDTO> updatePw(@RequestBody UserNewPasswordRequestDTO userNewPasswordRequestDTO) {
        return ResponseEntity.ok(userService.updatePassword(userNewPasswordRequestDTO));
    }

    @PostMapping("/resign")
    public ResponseEntity<Void> resign(HttpServletRequest request, @AuthenticationPrincipal User user)
            throws IOException, InterruptedException, GeneralSecurityException {
        userService.resign(request.getHeader(UserConstants.LOGIN_FROM_KEY), request.getHeader(UserConstants.TOKEN_KEY), user);
        return ResponseEntity.ok(null);
    }

    @PostMapping("/delete-all-images")
    public ResponseEntity<Void> deleteAllImage(HttpServletRequest request) {
        userResignService.deleteAllImages((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return ResponseEntity.ok(null);
    }

    @PostMapping("/refresh-token")
    public ResponseEntity<RefreshTokenResponseDTO> refreshAccessToken(HttpServletRequest request) throws IOException, InterruptedException {
        return ResponseEntity.ok(userService.refreshAccessToken(request.getHeader(UserConstants.LOGIN_FROM_KEY), request.getHeader(UserConstants.REFRESH_TOKEN_KEY)));
    }

    @GetMapping(value = "/google-login-callback")
    public void GoogleSignCallback(HttpServletRequest request, HttpServletResponse response) throws Exception {
        userService.googleSignCallback(request, response);
    }

    @PostMapping("/logout")
    public ResponseEntity<Void> logout(HttpServletRequest request) throws IOException, InterruptedException {
        userService.logout(request.getHeader(UserConstants.LOGIN_FROM_KEY), request.getHeader(UserConstants.TOKEN_KEY));
        return ResponseEntity.ok().build();
    }

    /**
     * 식사일기의 식사시간 통계 정보를 출력합니다.
     * @param user 스프링 SecurityContextHolder에 저장된 사용자 정보(https://docs.spring.io/spring-security/reference/servlet/integrations/mvc.html#mvc-authentication-principal)
     * @return
     */
    @GetMapping("/statistics")
    public ResponseEntity<DiaryStatisticsQueryDslResponseDTO> statistics(@AuthenticationPrincipal User user) {
        return ResponseEntity.ok(userService.getStatistics(user.getId()));
    }
}
