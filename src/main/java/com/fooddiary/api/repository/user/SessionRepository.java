package com.fooddiary.api.repository.user;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fooddiary.api.entity.user.Session;

public interface SessionRepository extends JpaRepository<Session, Integer> {

    Session findByToken(String token);
    Session findByRefreshToken(String refreshToken);

    Session findByTokenAndTokenTerminateAtGreaterThanEqual(String token, LocalDateTime TerminateAt);
}
