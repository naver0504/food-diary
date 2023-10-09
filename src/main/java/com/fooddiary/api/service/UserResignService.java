package com.fooddiary.api.service;

import com.amazonaws.services.s3.AmazonS3;
import com.fooddiary.api.common.utils.ImageUtils;
import com.fooddiary.api.entity.image.DayImage;
import com.fooddiary.api.entity.image.Image;
import com.fooddiary.api.entity.user.Status;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.DayImageRepository;
import com.fooddiary.api.repository.ImageRepository;
import com.fooddiary.api.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserResignService {

    private final DayImageRepository dayImageRepository;
    private final ImageRepository imageRepository;
    private final UserRepository userRepository;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;
    @Value("${cloud.aws.s3.dir}")
    private String basePath;

    @Async
    public void resign(final User user) {
        deleteAllImages(user);
        user.setStatus(Status.DELETE);
        userRepository.save(user);
    }

    public void deleteAllImages(final User user) {
        Integer id = -1;
        /*
        while(true) {
            List<DayImage> dayImageList = dayImageRepository.findByUserIdAndLimit(user.getId(), id, PageRequest.of(0, 10));
            if (dayImageList.isEmpty()) {
                break;
            }
            id = dayImageList.get(dayImageList.size() - 1).getId();


            for (DayImage dayImage : dayImageList) {
                List<Image> imageList = dayImage.getImages();
                for (Image image : imageList) {
                    amazonS3.deleteObject(bucket, ImageUtils.getDirPath(basePath, user) + image.getStoredFileName());
                    imageRepository.delete(image);
                }
                amazonS3.deleteObject(bucket, ImageUtils.getDirPath(basePath, user) + dayImage.getThumbNailImagePath());
                dayImageRepository.delete(dayImage);
            }
        }
        
         */
        amazonS3.deleteObject(bucket, ImageUtils.getDirPath(basePath, user));
    }
}
