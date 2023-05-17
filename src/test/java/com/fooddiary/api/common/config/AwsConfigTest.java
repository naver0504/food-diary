package com.fooddiary.api.common.config;

import java.io.IOException;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectId;

@SpringBootTest
@ActiveProfiles("local")
@ExtendWith(SpringExtension.class)
public class AwsConfigTest {

    @Value("${cloud.aws.s3.bucket}")
    String bucket;
    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;
    @Autowired
    AmazonS3 amazonS3;

    /**
     * AWS s3에 접속가능한지 확인합니다.
     * @throws IOException
     */
    @Test
    void accessTest() throws IOException {
        final S3ObjectId s3ObjectId = new S3ObjectId(bucket, "production/lesserafim.JPG");
        final GetObjectRequest objectRequest = new GetObjectRequest(s3ObjectId);
        final int available = amazonS3.getObject(objectRequest).getObjectContent().available();
        Assertions.assertEquals(available, 0);
    }

}
