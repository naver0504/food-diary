package com.fooddiary.api.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fooddiary.api.entity.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.jetbrains.annotations.NotNull;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class ImageUtils {

    public static final int THUMBNAIL_WIDTH = 100;
    public static final int THUMBNAIL_HEIGHT = 100;


    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @NotNull
    public static String getDirPath(final User user) {
        final String dirPath = user.getId() + "/";
        return dirPath;

    }

    @NotNull
    public static String createImageName(final String originalFilename) {
        final String storeFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        return storeFilename;
    }

    public String createThumbnailName(final MultipartFile file, final User user) {
        final String originalFilename = file.getOriginalFilename();
        final String storeFilename = "t_"+ UUID.randomUUID().toString() + "_" + originalFilename;
        final String fileContentType = getFileContentType(file.getContentType());

        final BufferedImage originalImage;

        try {
            originalImage = ImageIO.read(file.getInputStream());
        } catch (IOException e) {
            log.error("IOException ", e);
            throw new RuntimeException(e.getMessage());
        }

        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();


        try {


            Thumbnails.of(originalImage)
                    .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                    .outputFormat(fileContentType)
                    .toOutputStream(outputStream);



        } catch (IOException e) {
            log.error("IOException ", e);
            throw new RuntimeException(e.getMessage());
        }

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        try {
            ObjectMetadata metadata = new ObjectMetadata();

            final String dirPath = ImageUtils.getDirPath(user);
            amazonS3.putObject(bucket, dirPath+storeFilename, inputStream, metadata);
        } catch (AmazonServiceException e) {
            log.error("AmazonServiceException ", e);
            throw new RuntimeException(e.getMessage());
        } catch (SdkClientException e) {
            log.error("SdkClientException ", e);
            throw new RuntimeException(e.getMessage());
        }


        return storeFilename;
    }

    private String getFileContentType(String contentType) {
        if (contentType == "image/jpeg") {
            return "jpg";
        } else if (contentType == "image/png") {
            return "png";
        } else if (contentType == "image/gif") {
            return "gif";
        } else {
            return "jpg";
        }
    }
}
