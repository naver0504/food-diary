package com.fooddiary.api.repository.user;

import java.util.List;

import com.fooddiary.api.entity.user.CreatePath;
import org.springframework.data.jpa.repository.JpaRepository;

import com.fooddiary.api.entity.user.Status;
import com.fooddiary.api.entity.user.User;

public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findByEmail(String email);
    List<User> findByEmailAndCreatePathAndStatus(String email, CreatePath createPath, Status status);


    User findByEmailAndStatus(String email, Status status);
}
