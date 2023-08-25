package com.fooddiary.api.controller;

import com.fooddiary.api.dto.request.UserLoginRequestDTO;
import com.fooddiary.api.dto.request.UserNewPwRequestDTO;
import com.fooddiary.api.dto.request.UserNewRequestDTO;
import com.fooddiary.api.dto.response.UserResponseDTO;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.UserService;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private static final String MAIL_NAME = "email";
    private static final String TOKEN_NAME = "token";
    private final UserService userService;

    @GetMapping("/is-login")
    public ResponseEntity<HttpStatus> isLogin(HttpServletRequest request) {
        final User user = userService.getValidUser(request.getHeader(MAIL_NAME), request.getHeader(TOKEN_NAME));
        return user == null ? ResponseEntity.badRequest().build() : ResponseEntity.ok().build();
    }

    @PostMapping("/new")
    public ResponseEntity<UserResponseDTO> createUser(@RequestBody
                                                      UserNewRequestDTO userDto) {
        final String token = userService.createUser(userDto);
        final UserResponseDTO userResponseDto = new UserResponseDTO();
        userResponseDto.setToken(token);
        return ResponseEntity.ok(userResponseDto);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDTO> loginUser(@RequestBody
                                                     UserLoginRequestDTO userDto) {
        return ResponseEntity.ok(userService.loginUser(userDto));
    }

    @PostMapping("/reset-pw")
    public ResponseEntity<UserResponseDTO> resetPw() {
        return ResponseEntity.ok(userService.resetPw());
    }

    @PostMapping("/new-pw")
    public ResponseEntity<Void> updatePw(@RequestBody UserNewPwRequestDTO userNewPwRequestDTO) {
        userService.updatePw(userNewPwRequestDTO.getPw());
        return ResponseEntity.ok().build();
    }
}
