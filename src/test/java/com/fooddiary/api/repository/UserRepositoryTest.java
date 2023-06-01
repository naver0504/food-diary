package com.fooddiary.api.repository;

import com.fooddiary.api.entity.CreatePath;
import com.fooddiary.api.entity.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


@SpringBootTest
@ActiveProfiles("local")
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    @Transactional
    void query_test() {
        User user = new User();
        user.setCreatePath(CreatePath.GOOGLE);
        user.setEmail("jasuil@daum.net");
        user.setName("성일짱");
        userRepository.save(user);
        Assertions.assertNotEquals(userRepository.count(), 0);
    }
}
