package com.fooddiary.api;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.fooddiary.api.common.constants.Profiles;

@SpringBootTest
@ActiveProfiles(Profiles.TEST)
public class ApiApplicationTests {
}
