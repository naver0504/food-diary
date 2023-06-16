package com.fooddiary.api.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.fooddiary.api.dto.request.CreateUserRequestDto;
import com.fooddiary.api.dto.response.CreateUserResponseDto;
import com.fooddiary.api.service.UserService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    @GetMapping("/test") // 이것은 나중에 지우도록 하겠습니다.
    public void test() throws Exception {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        log.debug("call /test");
    }

    @PostMapping("/new")
    public ResponseEntity<CreateUserResponseDto> createUser(@RequestBody
                                                            CreateUserRequestDto userDto) {
        final String token = userService.createUser(userDto);
        final CreateUserResponseDto createUserResponseDto = new CreateUserResponseDto();
        createUserResponseDto.setToken(token);
        return ResponseEntity.ok(createUserResponseDto);
    }
}
