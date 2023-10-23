package com.fooddiary.api.service;

import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.repository.user.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;


import java.io.*;



@SpringBootTest
@ActiveProfiles(Profiles.TEST)
class ImageServiceTest {


    @Autowired
    UserRepository userRepository;

    @Test
    @Transactional
    public void imgServiceTest() throws IOException {


    }


}