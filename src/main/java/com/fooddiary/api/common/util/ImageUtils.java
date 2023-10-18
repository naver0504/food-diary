package com.fooddiary.api.common.util;

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
import java.io.InputStream;
import java.util.Objects;
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

    public static ByteArrayOutputStream createThumbnailImage(final MultipartFile file, final User user, final AmazonS3 amazonS3, final String bucket, final String basePath) {
        final String originalFilename = file.getOriginalFilename();
        final String fileContentType = getFileContentType(Objects.requireNonNull(file.getContentType()));
        final ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        final BufferedImage originalImage;

        try (InputStream fileInputStream = file.getInputStream()) {
            originalImage = ImageIO.read(fileInputStream);

            Thumbnails.of(originalImage)
                    .size(THUMBNAIL_WIDTH, THUMBNAIL_HEIGHT)
                    .keepAspectRatio(false)
                    .outputFormat(fileContentType)
                    .toOutputStream(outputStream);
        } catch (IOException e) {
            log.error("IOException {}", e.getMessage());
            throw new RuntimeException(e.getMessage());
        }
        return outputStream;
    }

    public static String getFileContentType(String contentType) {
        return switch (contentType) {
            case "image/jpeg" -> "jpg";
            case "image/png" -> "png";
            case "image/gif" -> "gif";
            default -> "jpg";
        };
    }
}
