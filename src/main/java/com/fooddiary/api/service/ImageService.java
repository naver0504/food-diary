package com.fooddiary.api.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fooddiary.api.common.utils.ImageUtils;
import com.fooddiary.api.entity.image.Image;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.DayImageRepository;
import com.fooddiary.api.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final DayImageRepository dayImageRepository;

    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public List<Image> storeImage(final List<MultipartFile> files, final LocalDateTime localDateTime, final User user, final double longitude, final double latitude,final String basePath) throws IOException {

        final List<Image> images = new ArrayList<>();
        Image firstImage = null;

        for (int i = 0; i<files.size(); i++) {
            final MultipartFile file = files.get(i);

            //파일 명 겹치면 안되므로 UUID + '-' + 원래 파일 이름으로 저장

            final String storeFilename = ImageUtils.createImageName(file.getOriginalFilename());
            final Image image = Image.createImage(localDateTime, storeFilename, longitude, latitude);
            if (i == 0) {
                firstImage = image;
            } else {
                firstImage.addChildImage(image);
            }
            final int userId = user.getId();


            //S3에 저장하는 로직
            try {
                ObjectMetadata metadata = new ObjectMetadata();

                final String dirPath = ImageUtils.getDirPath(basePath, user);
                int count = dayImageRepository.getDayImageCount(userId);
                if(count == 0) {
                    amazonS3.putObject(bucket, dirPath, new ByteArrayInputStream(new byte[0]), new ObjectMetadata());
                }

                amazonS3.putObject(bucket, dirPath+storeFilename, file.getInputStream(), metadata);
            } catch (AmazonServiceException e) {
                log.error("AmazonServiceException ", e);
                throw new RuntimeException(e.getMessage());
            } catch (SdkClientException e) {
                log.error("SdkClientException ", e);
                throw new RuntimeException(e.getMessage());
            } catch (IOException e) {
                log.error("IOException ", e);
                throw new RuntimeException(e.getMessage());
            }


            final Image saveImage = imageRepository.save(image);
            images.add(saveImage);
        }
        return images;

    }

}
