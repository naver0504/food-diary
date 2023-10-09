package com.fooddiary.api.service;

import com.fooddiary.api.common.exception.BizException;
import com.fooddiary.api.dto.request.SaveImageRequestDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.image.Image;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.diary.DiaryQuerydslRepository;
import com.fooddiary.api.repository.diary.DiaryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final DiaryQuerydslRepository diaryQuerydslRepository;
    private final ImageService imageService;
    @Value("${cloud.aws.s3.dir}")
    private String basePath;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public int createDiary(final List<MultipartFile> files, final SaveImageRequestDTO saveImageRequestDTO, final User user) {
        final LocalDateTime dateTime = saveImageRequestDTO.getCreateTime();
        final int year = dateTime.getYear();
        final int month = dateTime.getMonthValue();
        final int day = dateTime.getDayOfMonth();
        final int todayDiaryCount = diaryRepository.getByYearAndMonthAndDayCount(year, month, day, user.getId());

        if (todayDiaryCount >= 10) {
            throw new BizException("register only 10 per day");
        }
        final List<Image> images;

        images = imageService.storeImage(files, user, saveImageRequestDTO);

        final Diary newDiary = Diary.createDiaryImage(images, dateTime, user);
        diaryRepository.save(newDiary);

        return newDiary.getId();
    }

}
