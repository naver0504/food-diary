package com.fooddiary.api.repository;

import com.fooddiary.api.common.config.SecurityConfig;
import com.fooddiary.api.entity.session.Session;
import com.fooddiary.api.entity.user.CreatePath;
import com.fooddiary.api.entity.user.Status;
import com.fooddiary.api.entity.user.User;
import jakarta.persistence.EntityManager;
import org.junit.Assert;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Arrays;

@SpringBootTest
@ActiveProfiles("test")
public class SessionRepositoryTest {

    @Autowired
    UserRepository userRepository;
    @Autowired
    SessionRepository sessionRepository;
    @Autowired
    PasswordEncoder passwordEncoder;
    @Autowired
    EntityManager entityManager;

    /**
     * 회원가입후 로그인된 상황을 가정합니다.
     */
    @Test
    @Transactional
    void get_session() {
        User user = new User();
        user.setEmail("jasuil@naver.com");
        user.setName("성일짱");

        userRepository.save(user);

        Session session = new Session();
        session.setToken(passwordEncoder.encode(user.getEmail()+LocalDateTime.now().plusDays(1)));
        session.setTerminateAt(LocalDateTime.now().plusDays(1));
        session.setUser(user);

        sessionRepository.save(session);

        entityManager.flush();
        entityManager.clear();

        user = userRepository.findById(session.getUser().getId()).orElseGet(() -> new User());
        Assertions.assertNotNull(user);
    }
}
