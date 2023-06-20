package com.fooddiary.api.repository;

import java.time.LocalDateTime;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.entity.session.Session;
import com.fooddiary.api.entity.user.User;

import jakarta.persistence.EntityManager;

@SpringBootTest
@ActiveProfiles(Profiles.TEST)
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

        final Session session = new Session();
        session.setToken(passwordEncoder.encode(user.getEmail() + LocalDateTime.now().plusDays(1)));
        session.setTerminateAt(LocalDateTime.now().plusDays(1));
        session.setUser(user);

        sessionRepository.save(session);

        // 다음 조회 전에 commit 해야 join이 동기화 된다.
        entityManager.flush();
        entityManager.clear();

        user = userRepository.findById(session.getUser().getId()).orElseGet(User::new);
        Assertions.assertNotNull(user);
        // fetch lazy
        Assertions.assertEquals(user.getSession().size(), 1);
        Assertions.assertTrue(sessionRepository.findByToken(session.getToken()).getTerminateAt()
                                               .isAfter(LocalDateTime.now()));
    }
}
