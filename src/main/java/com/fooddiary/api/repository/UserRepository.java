package com.fooddiary.api.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.fooddiary.api.entity.user.Status;
import com.fooddiary.api.entity.user.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findByEmail(String email);

    User findByEmailAndStatus(String email, Status status);
}
