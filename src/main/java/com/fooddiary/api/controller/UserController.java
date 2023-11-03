package com.fooddiary.api.controller;

import java.io.IOException;
import java.security.GeneralSecurityException;

import com.fooddiary.api.common.constants.UserConstants;
import com.fooddiary.api.dto.request.user.UserLoginRequestDTO;
import com.fooddiary.api.dto.request.user.UserNewPasswordRequestDTO;
import com.fooddiary.api.dto.request.user.UserNewRequestDTO;
import com.fooddiary.api.dto.request.user.UserResetPasswordRequestDTO;
import com.fooddiary.api.dto.response.user.UserInfoResponseDTO;
import com.fooddiary.api.dto.response.user.UserNewPasswordResponseDTO;
import com.fooddiary.api.dto.response.user.UserResponseDTO;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.user.UserResignService;
import com.fooddiary.api.service.user.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

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
        final User user = userService.getValidUser(request.getHeader(UserConstants.LOGIN_FROM_KEY), request.getHeader(UserConstants.TOKEN_KEY));
        return user == null ? ResponseEntity.badRequest().build() : ResponseEntity.ok().build();
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
    public ResponseEntity<Void> resign(HttpServletRequest request)
            throws IOException, InterruptedException, GeneralSecurityException {
        userService.resign(request.getHeader(UserConstants.LOGIN_FROM_KEY), request.getHeader(UserConstants.TOKEN_KEY));
        return ResponseEntity.ok(null);
    }

    @PostMapping("/delete-all-images")
    public ResponseEntity<Void> deleteAllImage(HttpServletRequest request) {
        userResignService.deleteAllImages((User) SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        return ResponseEntity.ok(null);
    }
}
