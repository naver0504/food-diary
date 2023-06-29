package com.fooddiary.api.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.CannedAccessControlList;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fooddiary.api.entity.image.Image;
import com.fooddiary.api.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Transactional
    public List<Image> storeImage(List<MultipartFile> files, LocalDateTime localDateTime) throws IOException {

        List<Image> images = new ArrayList<>();

        for (MultipartFile file : files) {

            String originalFilename = file.getOriginalFilename();
            String storeFilename = UUID.randomUUID()+"-"+ originalFilename.split("\\.")[0];
            String ext = originalFilename.split("\\.")[1];
            Image image = Image.createImage(localDateTime, storeFilename);
            String contentType = "";

            //content-type을 지정해서 올려주지 않으면 자동으로 "application/octet-stream"으로 고정이 되서 링크 클릭시 웹에서 열리는게 아니라 자동 다운이 시작됨.
            contentType = getContentType(ext);

            try {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentType(contentType);
                amazonS3.putObject(new PutObjectRequest(bucket, storeFilename, file.getInputStream(), metadata)
                        .withCannedAcl(CannedAccessControlList.PublicRead));

            } catch (AmazonServiceException e) {
                log.info("AmazonServiceException = {}", e.getMessage());
                throw new RuntimeException(e.getMessage());
            } catch (SdkClientException e) {
                log.info("SdkClientException = {}", e.getMessage());
                throw new RuntimeException(e.getMessage());
            }

                Image saveImage = imageRepository.save(image);
                images.add(saveImage);
            }
        return images;

    }

    private  String getContentType(String ext) {
        String contentType = "";
        switch (ext) {
            case "jpeg":
                contentType = "image/jpeg";
                break;
            case "png":
                contentType = "image/png";
                break;
        }
        return contentType;
    }

}
