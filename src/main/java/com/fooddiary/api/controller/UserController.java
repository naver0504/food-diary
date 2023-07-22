package com.fooddiary.api.controller;

import com.fooddiary.api.dto.request.UserLoginRequestDto;
import com.fooddiary.api.dto.request.UserNewRequestDto;
import com.fooddiary.api.dto.response.UserResponseDto;
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
        User user = userService.getValidUser(request.getHeader(MAIL_NAME), request.getHeader(TOKEN_NAME));
        return user == null ? ResponseEntity.badRequest().build() : ResponseEntity.ok().build();
    }

    @PostMapping("/new")
    public ResponseEntity<UserResponseDto> createUser(@RequestBody
                                                      UserNewRequestDto userDto) {
        final String token = userService.createUser(userDto);
        final UserResponseDto userResponseDto = new UserResponseDto();
        userResponseDto.setToken(token);
        return ResponseEntity.ok(userResponseDto);
    }

    @PostMapping("/login")
    public ResponseEntity<UserResponseDto> loginUser(@RequestBody
                                                     UserLoginRequestDto userDto) {
        return ResponseEntity.ok(userService.loginUser(userDto));
    }

    @GetMapping("/pw-reset/{email}")
    public ResponseEntity<UserResponseDto> passwordReset(@PathVariable(name = "email") String email) {
        return ResponseEntity.ok(userService.passwordReset(email));
    }

    @PutMapping("/pw")
    public void updatePw(@RequestBody String pw) {
        userService.updatePw(pw);
    }
}
