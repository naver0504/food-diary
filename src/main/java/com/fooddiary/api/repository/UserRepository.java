package com.fooddiary.api.repository;


import com.fooddiary.api.entity.user.Status;
import com.fooddiary.api.entity.user.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserRepository extends JpaRepository<User, Integer> {
    List<User> findByEmail(String email);
    User findByEmailAndStatus(String email, Status status);
}
