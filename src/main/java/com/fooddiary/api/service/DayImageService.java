package com.fooddiary.api.service;


import com.amazonaws.services.s3.AmazonS3;
import com.fooddiary.api.dto.response.DayImageDto;
import com.fooddiary.api.dto.response.DayImagesDto;
import com.fooddiary.api.entity.image.DayImage;
import com.fooddiary.api.entity.image.Image;
import com.fooddiary.api.entity.image.Time;
import com.fooddiary.api.repository.DayImageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URL;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Transactional(readOnly = true)
public class DayImageService {

    private final DayImageRepository dayImageRepository;
    private final ImageService imageService;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Transactional
    public void saveImage(List<MultipartFile> files, LocalDateTime dateTime) throws IOException {

        int year = dateTime.getYear();
        int month = dateTime.getMonthValue();
        int day = dateTime.getDayOfMonth();
        DayImage dayImage = dayImageRepository.findByYearAndMonthAndDay(year, month, day);
        List<Image> images = imageService.storeImage(files, dateTime);

        /***
         * 해당 날짜에 사진들이 있는 지 확인
         * 있다면 사진들을 추가하고 첫 번째 사진 썸네일로
         * 없다면 해당 날짜 이미지 엔티티 생성 후 사진 썸네일 설정
         */
        if (dayImage == null) {
            DayImage newDayImage = DayImage.createDayImage(images, dateTime);
            dayImageRepository.save(newDayImage);
        } else {

            /**
             * 변경 감지로 알아서 update 쿼리
             */
            dayImage.setImages(images);
            dayImage.setThumbNailImage(images.get(0));
        }


    }

    /***
     *
     * 하루 사진 받기
     *
     */

    public List<DayImageDto> getDayImage(int year, int month, int day) {

        DayImage dayImage = dayImageRepository.findByYearAndMonthAndDay(year, month, day);
        List<Image> images = dayImage.getImages();
        List<DayImageDto> dayImageDto = new ArrayList<>();
        for (Image storedImage : images) {
            URL url = amazonS3.getUrl(bucket, storedImage.getStoredFileName());
            String timeStatus = storedImage.getTimeStatus().getCode();
            dayImageDto.add(new DayImageDto(url, timeStatus));
        }
        return dayImageDto;
    }

    public List<DayImagesDto> getDayImages(int year, int month) {
        List<DayImage> dayImages = dayImageRepository.findByYearAndMonth(year, month);
        List<DayImagesDto> dayImagesDtos = new ArrayList<>();
        for (DayImage dayImage : dayImages) {
            URL url = amazonS3.getUrl(bucket, dayImage.getThumbNailImage().getStoredFileName());
            Time time = dayImage.getTime();
            dayImagesDtos.add(new DayImagesDto(url, time));
        }
        return dayImagesDtos;

    }


}
