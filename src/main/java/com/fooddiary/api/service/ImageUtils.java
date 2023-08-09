package com.fooddiary.api.service;

import com.fooddiary.api.entity.user.User;
import org.jetbrains.annotations.NotNull;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ImageUtils {

    public static final int THUMBNAIL_WIDTH = 1000;
    public static final int THUMBNAIL_HEIGHT = 1000;

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
}
