package com.fooddiary.api.common.utils;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fooddiary.api.entity.user.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.coobird.thumbnailator.Thumbnails;
import org.jetbrains.annotations.NotNull;
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


    public static final int THUMBNAIL_WIDTH = 44;
    public static final int THUMBNAIL_HEIGHT = 44;

    @NotNull
    public static String getDirPath(final String basePath, final User user) {


        final String dirPath = basePath + "/" + user.getId() + "/";
        return dirPath;

    }


    @NotNull
    public static String createImageName(final String originalFilename) {
        final String storeFilename = UUID.randomUUID().toString() + "_" + originalFilename;
        return storeFilename;
    }

    public static String createThumbnailImage(final MultipartFile file, final User user, final AmazonS3 amazonS3, final String bucket, final String basePath) {
        final String originalFilename = file.getOriginalFilename();
        final String storeThumbnailFilename = "t_"+ UUID.randomUUID().toString() + "_" + originalFilename;
        final String fileContentType = getFileContentType(file.getContentType());
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();

        final BufferedImage originalImage;

        try {
            originalImage = ImageIO.read(file.getInputStream());


            Thumbnails.of(originalImage)
                    .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                    .keepAspectRatio(false)
                    .outputFormat(fileContentType)
                    .toOutputStream(outputStream);



        } catch (IOException e) {
            log.error("IOException {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }

        final ByteArrayInputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());


        try {
            final ObjectMetadata metadata = new ObjectMetadata();
            final String dirPath = ImageUtils.getDirPath(basePath, user);
            amazonS3.putObject(bucket, dirPath+storeThumbnailFilename, inputStream, metadata);
        } catch (AmazonServiceException e) {
            log.error("AmazonServiceException ", e);
            throw new RuntimeException(e.getMessage());
        } catch (SdkClientException e) {
            log.error("SdkClientException ", e);
            throw new RuntimeException(e.getMessage());
        }


        return storeThumbnailFilename;
    }

    public static String getFileContentType(String contentType) {
        if (contentType.equals( "image/jpeg")) {
            return "jpg";
        } else if (contentType.equals("image/png")) {
            return "png";
        } else if (contentType.equals("image/gif")) {
            return "gif";
        } else {
            return "jpg";
        }
    }
}
