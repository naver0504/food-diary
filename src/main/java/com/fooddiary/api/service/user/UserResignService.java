package com.fooddiary.api.service.user;

import com.amazonaws.services.s3.AmazonS3;
import com.fooddiary.api.common.util.ImageUtils;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.diary.DiaryTag;
import com.fooddiary.api.entity.diary.Image;
import com.fooddiary.api.entity.diary.Tag;
import com.fooddiary.api.entity.user.Status;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.ImageRepository;
import com.fooddiary.api.repository.diary.DiaryRepository;
import com.fooddiary.api.repository.diary.DiaryTagRepository;
import com.fooddiary.api.repository.diary.TagRepository;
import com.fooddiary.api.repository.user.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class UserResignService {

    private final DiaryRepository diaryRepository;
    private final DiaryTagRepository diaryTagRepository;
    private final TagRepository tagRepository;
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
                List<Tag> deletableTags = new ArrayList<>();
                diaryTags.forEach(diaryTag -> {
                    Tag tag = diaryTag.getTag();
                    tag.getDiaryTags().remove(diaryTag);
                    if (tag.getDiaryTags().isEmpty()) {
                        deletableTags.add(tag);
                    }
                });
                diary.setDiaryTags(null);
                diaryTagRepository.deleteAll(diaryTags);
                deletableTags.forEach(tag -> {
                    try {
                        tagRepository.delete(tag);
                    } catch (Exception e) {
                        log.info("can't delete tag, tag id: {}", tag.getId());
                    }
                });
                diaryRepository.delete(diary);
            }
        }

        amazonS3.deleteObject(bucket, ImageUtils.getDirPath(basePath, user));
    }
}
