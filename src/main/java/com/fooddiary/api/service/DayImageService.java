package com.fooddiary.api.service;


import com.amazonaws.services.s3.AmazonS3;
import com.fooddiary.api.FileStorageService;
import com.fooddiary.api.dto.response.DayImageDto;
import com.fooddiary.api.dto.response.DayImagesDto;
import com.fooddiary.api.dto.response.SaveImageResponseDto;
import com.fooddiary.api.entity.image.DayImage;
import com.fooddiary.api.entity.image.Image;
import com.fooddiary.api.entity.image.Time;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.DayImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import static com.fooddiary.api.dto.response.SaveImageResponseDto.*;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
@Slf4j
public class DayImageService {

    private final DayImageRepository dayImageRepository;
    private final ImageService imageService;
    private final FileStorageService fileStorageService;
    private final ImageUtils imageUtils;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    @Transactional
    public SaveImageResponseDto saveImage(final List<MultipartFile> files, final LocalDateTime dateTime,final User user) {



        final int year = dateTime.getYear();
        final int month = dateTime.getMonthValue();
        final int day = dateTime.getDayOfMonth();
        final DayImage dayImage = dayImageRepository.findByYearAndMonthAndDay(year, month, day, user.getId());
        final List<Image> images;

        try {
            images = imageService.storeImage(files, dateTime, user);
        } catch (IOException e) {
            throw new RuntimeException(e.getMessage());
        }

        /***
         * 해당 날짜에 사진들이 있는 지 확인
         * 있다면 사진들을 추가하고 첫 번째 사진 썸네일로
         * 없다면 해당 날짜 이미지 엔티티 생성 후 사진 썸네일 설정
         */
        if (dayImage == null) {
            final DayImage newDayImage = DayImage.createDayImage(images, dateTime, user);
            dayImageRepository.save(newDayImage);
            newDayImage.updateThumbNailImageName(imageUtils.createThumbnailName(files.get(0), user, amazonS3, bucket));


        } else {
            /**
             * 변경 감지로 알아서 update 쿼리
             */
            dayImage.setImages(images);
            final String originalThumbnailPath = dayImage.getThumbNailImagePath();
            final String dirPath = ImageUtils.getDirPath(user);

            fileStorageService.deleteImage(dirPath + originalThumbnailPath);
            dayImage.updateThumbNailImageName(imageUtils.createThumbnailName(files.get(0), user, amazonS3, bucket));

        }


        return SaveImageResponseDto.builder()
                .status(Status.SUCCESS)
                .build();

    }

    /***
     *
     * 하루 사진 받기
     *
     */

    public List<DayImageDto> getDayImage(int year, int month, int day, User user) {

        final DayImage dayImage = dayImageRepository.findByYearAndMonthAndDay(year, month, day, user.getId());
        final List<Image> images = dayImage.getImages();
        final List<DayImageDto> dayImageDto = new ArrayList<>();
        final String dirPath = ImageUtils.getDirPath(user);

        for (Image storedImage : images) {
            byte[] bytes;
            try {
                bytes = fileStorageService.getObject(dirPath + storedImage.getStoredFileName());
            } catch (IOException e) {
                log.error("IOException ", e);
                throw new RuntimeException(e);
            }
            final String timeStatus = storedImage.getTimeStatus().getCode();
            dayImageDto.add(
                    DayImageDto.builder()
                    .bytes(bytes)
                    .timeStatus(timeStatus)
                    .id(storedImage.getId())
                    .build()
            );
        }
        return dayImageDto;
    }

    public List<DayImagesDto> getDayImages(final int year, final int month, final User user)  {
        final List<DayImage> dayImages = dayImageRepository.findByYearAndMonth(year, month, user.getId());
        final List<DayImagesDto> dayImagesDtos = new ArrayList<>();
        final String dirPath = ImageUtils.getDirPath(user);
        for (DayImage dayImage : dayImages) {
            byte[] bytes;
            try {
                bytes = fileStorageService.getObject(dirPath + dayImage.getThumbNailImagePath());
            } catch (IOException e) {
                log.error("IOException ", e);
                throw new RuntimeException(e);
            }
            final Time time = dayImage.getTime();
            dayImagesDtos.add(
                    DayImagesDto.builder()
                            .id(dayImage.getId())
                            .time(time)
                            .bytes(bytes)
                            .build()
            );

        }
        return dayImagesDtos;

    }






}
