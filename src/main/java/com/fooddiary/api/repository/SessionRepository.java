package com.fooddiary.api.repository;

import java.time.LocalDateTime;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.fooddiary.api.entity.session.Session;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {

    Session findByToken(String token);

    Session findByUserIdAndTokenAndTerminateAtGreaterThanEqual(Integer userId, String token,
                                                               LocalDateTime TerminateAt);
}
