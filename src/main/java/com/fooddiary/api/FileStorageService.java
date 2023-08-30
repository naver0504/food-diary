package com.fooddiary.api;

import java.io.IOException;
import java.io.InputStream;

import org.apache.tomcat.util.http.fileupload.IOUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.GetObjectRequest;
import com.amazonaws.services.s3.model.S3ObjectId;

@Service
public class FileStorageService {
    @Value("${cloud.aws.s3.bucket}")
    String bucket;
    @Value("${cloud.aws.credentials.accessKey}")
    private String accessKey;
    @Autowired
    AmazonS3 amazonS3;

    /**
     *
     * @param key key는 s3 bucket에 저장된 경로/파일 입니다.
     * @return
     * @throws IOException
     */
    public byte[] getObject(final String key) throws IOException {
        // todo - mysql이용하여 s3용 key값으로 변환해야 함
        final S3ObjectId s3ObjectId = new S3ObjectId(bucket, key);
        final GetObjectRequest objectRequest = new GetObjectRequest(s3ObjectId);
        final InputStream inputStream = amazonS3.getObject(objectRequest).getObjectContent();
        final int length = (int) amazonS3.getObject(objectRequest).getObjectMetadata().getContentLength();

        final byte[] buffer = new byte[length];
        IOUtils.readFully(inputStream, buffer, 0, length);
        return buffer;
    }

    /***
     *
     * 파일 삭제
     *
     */
    public void deleteImage(final String key) {

        boolean isObjectExist = amazonS3.doesObjectExist(bucket, key);
        if (isObjectExist) {
            amazonS3.deleteObject(bucket, key);
        }



    }
}
