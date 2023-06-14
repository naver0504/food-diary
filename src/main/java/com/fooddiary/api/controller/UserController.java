package com.fooddiary.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fooddiary.api.dto.UserDto;
import com.fooddiary.api.service.UserService;

import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/test")
    public void test() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        System.out.println(1);
    }

    @PostMapping("/new")
    public ResponseEntity<String> createUser(@RequestBody UserDto userDto) {
        final String token = userService.createUser(userDto);
        return ResponseEntity.ok(token);
    }
}
