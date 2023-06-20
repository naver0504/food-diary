package com.fooddiary.api.service;

import com.fooddiary.api.entity.session.Session;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.SessionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class SessionService {
    private final SessionRepository sessionRepository;
    private final PasswordEncoder passwordEncoder;

    public Session createSession(User user) {
        final LocalDateTime now = LocalDateTime.now();
        final Session session = new Session();
        session.setUser(user);
        session.setToken(passwordEncoder.encode(user.getEmail() + now));
        session.setTerminateAt(now.plusDays(1));

        return sessionRepository.save(session);
    }

    public Session getSession(Integer userId, String token) {
        return sessionRepository.findByUserIdAndTokenAndTerminateAtGreaterThanEqual(userId, token,
                                                                                    LocalDateTime.now());
    }

    public void deleteSession(Session session) {
        sessionRepository.delete(session);
    }
}
