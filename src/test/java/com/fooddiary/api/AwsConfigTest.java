package com.fooddiary.api;

import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStream;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3Client;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectId;
import com.fooddiary.api.common.config.AwsConfig;

@SpringBootTest
@ExtendWith(SpringExtension.class)
public class AwsConfigTest {

    @Value("${cloud.aws.s3.bucket}")
    String bucket;
    @Value("${cloud.aws.credentials.secretKey}")
    private String secretKey;
    @Autowired
    AmazonS3 amazonS3;

    @Test
    void accessTest() {
        try {
            S3ObjectId s3ObjectId = new S3ObjectId(bucket, secretKey);
            GetObjectRequest objectRequest = new GetObjectRequest(s3ObjectId);

            AmazonS3Client client = new AmazonS3Client();
            client.set

            ResponseBytes<GetObjectResponse> objectBytes = s3.getObjectAsBytes(objectRequest);
            byte[] data = objectBytes.asByteArray();

            // Write the data to a local file.
            File myFile = new File(path );
            OutputStream os = new FileOutputStream(myFile);
            os.write(data);
            System.out.println("Successfully obtained bytes from an S3 object");
            os.close();

        } catch (Exception ex) {
            ex.printStackTrace();
            System.exit(1);
        }
    }
}
