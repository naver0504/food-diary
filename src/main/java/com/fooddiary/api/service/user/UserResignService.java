package com.fooddiary.api.service.user;

import com.amazonaws.services.s3.AmazonS3;
import com.fooddiary.api.common.utils.ImageUtils;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.diary.Image;
import com.fooddiary.api.entity.diary.DiaryTag;
import com.fooddiary.api.entity.user.Status;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.diary.DiaryTagRepository;
import com.fooddiary.api.repository.ImageRepository;
import com.fooddiary.api.repository.user.UserRepository;
import com.fooddiary.api.repository.diary.DiaryRepository;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class UserResignService {

    private final DiaryRepository diaryRepository;
    private final DiaryTagRepository diaryTagRepository;
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

        while(true) {
            List<Diary> diaryList = diaryRepository.findByUserIdAndLimit(user.getId(), id, PageRequest.of(0, 10));
            if (diaryList.isEmpty()) {
                break;
            }
            id = diaryList.get(diaryList.size() - 1).getId();

            for (Diary diary : diaryList) {
                List<Image> imageList = diary.getImages();
                for (Image image : imageList) {
                    amazonS3.deleteObject(bucket, ImageUtils.getDirPath(basePath, user) + image.getStoredFileName());
                    amazonS3.deleteObject(bucket, ImageUtils.getDirPath(basePath, user) + image.getThumbnailFileName());
                    imageRepository.delete(image);
                }
                List<DiaryTag> diaryTags = diary.getDiaryTags();
                diaryTags.forEach(tag -> tag.getTag().getDiaryTags().remove(tag));
                diary.setDiaryTags(null);
                diaryTagRepository.deleteAll(diaryTags);
                diaryRepository.delete(diary);
            }
        }

        amazonS3.deleteObject(bucket, ImageUtils.getDirPath(basePath, user));
    }
}
