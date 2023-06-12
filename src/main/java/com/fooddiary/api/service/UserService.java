package com.fooddiary.api.service;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fooddiary.api.dto.UserDto;
import com.fooddiary.api.entity.session.Session;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    public String createUser(UserDto userDto) {
        final User user = new User();
        user.setEmail(user.getEmail());
        user.setPw(passwordEncoder.encode(userDto.getPassword()));
        userRepository.save(user);

        final Session session = sessionService.createSession(user);
        return session.getToken();
    }

    /**
     * 1. 이메일 유효성 검증
     * 2. 식사일기에 직접 가입한 경우, token 만료를 확인
     * 2. kakao, google 등 외부로 가입한 경우 access token 만료를 확인
     * @param email request header 에서 가져옴
     * @param token request header 에서 가져옴
     * @return 유효하면 true
     */
    public boolean isValid(String email, String token) {
        // todo
        return true;
    }
}
