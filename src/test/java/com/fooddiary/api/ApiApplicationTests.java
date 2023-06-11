package com.fooddiary.api;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import com.amazonaws.services.dynamodbv2.AmazonDynamoDBClientBuilder;
import com.amazonaws.regions.Regions;
@SpringBootTest
@ActiveProfiles("test")
class ApiApplicationTests {
}
