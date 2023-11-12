package com.fooddiary.api.service.user;

import com.fooddiary.api.entity.user.Session;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.user.SessionRepository;
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
        session.setTokenTerminateAt(now.plusDays(1));
        session.setRefreshToken(passwordEncoder.encode(user.getEmail() + now.plusMonths(1L)));
        session.setRefreshTokenTerminateAt(now.plusMonths(1L));

        return sessionRepository.save(session);
    }

    public Session getSession(String token) {
        return sessionRepository.findByTokenAndTokenTerminateAtGreaterThanEqual(token, LocalDateTime.now());
    }

    public void deleteSession(Session session) {
        sessionRepository.delete(session);
    }
}
