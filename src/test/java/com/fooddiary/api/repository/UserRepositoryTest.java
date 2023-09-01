package com.fooddiary.api.repository;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.entity.user.CreatePath;
import com.fooddiary.api.entity.user.Status;
import com.fooddiary.api.entity.user.User;

import java.time.LocalDateTime;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles(Profiles.TEST)
public class UserRepositoryTest {

    @Autowired
    UserRepository userRepository;

    @Test
    @Transactional
    void query_test() {
        final LocalDateTime now = LocalDateTime.now();
        final User user = new User();
        user.setEmail("jasuil@daum.net");
        user.setPwUpdateAt(now);
        user.setPwUpdateDelayAt(now);

        userRepository.save(user);

        Assertions.assertEquals(user.getCreatePath(), CreatePath.NONE);
        Assertions.assertEquals(user.getStatus(), Status.ACTIVE);
        Assertions.assertNotNull(user.getCreateAt());
    }
}
