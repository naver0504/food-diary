package com.fooddiary.api.repository;


import com.fooddiary.api.entity.session.Session;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface SessionRepository extends JpaRepository<Session, Integer> {

    Session findByToken(String token);
}
