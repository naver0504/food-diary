package com.fooddiary.api.service;

import java.util.List;
import java.util.stream.Collectors;

import javax.annotation.Nullable;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import com.fooddiary.api.dto.request.CreateUserRequestDto;
import com.fooddiary.api.entity.session.Session;
import com.fooddiary.api.entity.user.Status;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.UserRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final SessionService sessionService;

    public String createUser(CreateUserRequestDto userDto) {
        final User user = new User();
        user.setEmail(userDto.getEmail());
        user.setName(userDto.getName());
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
     * @return 유효하면 User 객체반환
     */
    @Nullable
    public User getValidUser(String email, String token) {
        // todo
        if (!StringUtils.hasText(email) || !StringUtils.hasText(token)) {return null;}

        List<User> userList = userRepository.findByEmail(email);
        userList = userList.stream().filter(u -> u.getStatus() == Status.ACTIVE).collect(Collectors.toList());

        if (CollectionUtils.isEmpty(userList)) {return null;}

        final Session session = sessionService.getSession(userList.get(0).getId(), token);

        if (session != null) {
            userList.get(0).setSession(List.of(session));
            return userList.get(0);
        }

        return null;
    }
}
