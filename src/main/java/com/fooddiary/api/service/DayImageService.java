package com.fooddiary.api.service;


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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
@Slf4j
public class DayImageService {

    private final DayImageRepository dayImageRepository;
    private final ImageService imageService;
    private final FileStorageService fileStorageService;


    @Transactional
    public SaveImageResponseDto saveImage(List<MultipartFile> files, LocalDateTime dateTime,User user) {



        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        int day = dateTime.getDayOfMonth();
        DayImage dayImage = dayImageRepository.findByYearAndMonthAndDay(year, month, day, user.getId());
        List<Image> images = imageService.storeImage(files, dateTime, user);

        /***
         * 해당 날짜에 사진들이 있는 지 확인
         * 있다면 사진들을 추가하고 첫 번째 사진 썸네일로
         * 없다면 해당 날짜 이미지 엔티티 생성 후 사진 썸네일 설정
         */
        if (dayImage == null) {
            DayImage newDayImage = DayImage.createDayImage(images, dateTime);
            newDayImage.setUser(user);
            dayImageRepository.save(newDayImage);
        } else {

            /**
             * 변경 감지로 알아서 update 쿼리
             */
            dayImage.setImages(images);
            dayImage.setThumbNailImagePath(images.get(0).getStoredFileName());
        }

        SaveImageResponseDto saveImageResponseDto = new SaveImageResponseDto();
        saveImageResponseDto.setStatus(SaveImageResponseDto.Status.SUCCESS);
        return saveImageResponseDto;

    }

    /***
     *
     * 하루 사진 받기
     *
     */

    public List<DayImageDto> getDayImage(int year, int month, int day, User user) {

        DayImage dayImage = dayImageRepository.findByYearAndMonthAndDay(year, month, day, user.getId());
        List<Image> images = dayImage.getImages();
        List<DayImageDto> dayImageDto = new ArrayList<>();
        String dirPath = user.getId() + "/";

        for (Image storedImage : images) {
            byte[] bytes;
            try {
                bytes = fileStorageService.getObject(dirPath + storedImage.getStoredFileName());
            } catch (IOException e) {
                log.error("IOException ", e);
                throw new RuntimeException(e);
            }
            String timeStatus = storedImage.getTimeStatus().getCode();
            dayImageDto.add(new DayImageDto(bytes, timeStatus));
        }
        return dayImageDto;
    }

    public List<DayImagesDto> getDayImages(int year, int month, User user)  {
        List<DayImage> dayImages = dayImageRepository.findByYearAndMonth(year, month, user.getId());
        List<DayImagesDto> dayImagesDtos = new ArrayList<>();
        String dirPath = user.getId() + "/";
        for (DayImage dayImage : dayImages) {
            byte[] bytes;
            try {
                bytes = fileStorageService.getObject(dirPath + dayImage.getThumbNailImagePath());
            } catch (IOException e) {
                log.error("IOException ", e);
                throw new RuntimeException(e);
            }
            Time time = dayImage.getTime();
            dayImagesDtos.add(new DayImagesDto(bytes, time));
        }
        return dayImagesDtos;

    }


}
