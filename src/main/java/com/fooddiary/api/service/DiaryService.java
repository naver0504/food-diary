package com.fooddiary.api.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import com.fooddiary.api.FileStorageService;
import com.fooddiary.api.common.utils.ImageUtils;
import com.fooddiary.api.dto.response.ThumbNailImagesDTO;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fooddiary.api.common.exception.BizException;
import com.fooddiary.api.dto.request.SaveImageRequestDTO;
import com.fooddiary.api.dto.request.diary.NewDiaryRequestDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.image.Image;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.diary.DiaryQuerydslRepository;
import com.fooddiary.api.repository.diary.DiaryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final DiaryQuerydslRepository diaryQuerydslRepository;
    private final FileStorageService fileStorageService;
    private final ImageService imageService;
    @Value("${cloud.aws.s3.dir}")
    private String basePath;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public void createDiary(final List<MultipartFile> files, final NewDiaryRequestDTO newDiaryRequestDTO,
                            final User user) {
        final LocalDateTime dateTime = newDiaryRequestDTO.getCreateTime();
        final int year = dateTime.getYear();
        final int month = dateTime.getMonthValue();
        final int day = dateTime.getDayOfMonth();
        final int todayDiaryCount = diaryRepository.getByYearAndMonthAndDayCount(year, month, day,
                                                                                 user.getId());

        if (todayDiaryCount >= 10) {
            throw new BizException("register only 10 per day");
        }

        final Diary newDiary = Diary.createDiaryImage(dateTime, user);
        diaryRepository.save(newDiary);
        imageService.storeImage(newDiary, files, user, SaveImageRequestDTO.builder().diaryTime(
                newDiaryRequestDTO.getDiaryTime()).createTime(newDiaryRequestDTO.getCreateTime()).build());
    }

    public void addImages(final int diaryId, final List<MultipartFile> files, final NewDiaryRequestDTO newDiaryRequestDTO, final User user) {
        if (diaryRepository.getDiaryImagesCount(diaryId) + files.size() > 5) {
            throw new BizException("we allow max 5 images");
        }
        Diary diary = diaryRepository.findById(diaryId).get();
        imageService.storeImage(diary, files, user, SaveImageRequestDTO.builder().diaryTime(
                newDiaryRequestDTO.getDiaryTime()).createTime(newDiaryRequestDTO.getCreateTime()).build());
    }

    public List<ThumbNailImagesDTO> getMonthlyImages(final @RequestParam int year, final @RequestParam int month, final @AuthenticationPrincipal User user) throws IOException {
        List<ThumbNailImagesDTO> thumbNailImagesDTOList = new LinkedList<>();
        List<Diary> diaryList = diaryRepository.findByYearAndMonth(year, month, user.getId());
        for (Diary diary : diaryList) {
            if (!diary.getImages().isEmpty()) {
                Image image = diary.getImages().get(0);
                ThumbNailImagesDTO thumbNailImagesDTO = new ThumbNailImagesDTO();
                thumbNailImagesDTO.setId(image.getId());
                thumbNailImagesDTO.setTime(diary.getTime());
                thumbNailImagesDTO.setBytes(fileStorageService.getObject(ImageUtils.getDirPath(basePath, user) + image.getThumbnailFileName()));
                thumbNailImagesDTOList.add(thumbNailImagesDTO);
            }
        }
        return thumbNailImagesDTOList;
    }

}
