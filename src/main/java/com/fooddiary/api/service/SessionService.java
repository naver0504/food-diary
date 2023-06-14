package com.fooddiary.api.service;

import java.time.LocalDateTime;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.fooddiary.api.entity.session.Session;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.SessionRepository;

import lombok.RequiredArgsConstructor;

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

    public boolean isValidToken(String token) {
        return isValidSession(sessionRepository.findByToken(token));
    }

    public boolean isValidSession(Session session) {
        if (session == null) {return false;}
        return !LocalDateTime.now().isAfter(session.getTerminateAt());
    }
}
