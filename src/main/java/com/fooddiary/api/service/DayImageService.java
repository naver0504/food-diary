package com.fooddiary.api.service;


import com.amazonaws.services.s3.AmazonS3;
import com.fooddiary.api.dto.response.DayImageDto;
import com.fooddiary.api.entity.image.DayImage;
import com.fooddiary.api.entity.image.Image;
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

        if (dayImage == null) {
            DayImage newDayImage = DayImage.createDayImage(images, dateTime);
            dayImageRepository.save(dayImage);
        } else {
            for (Image image : images) {
                dayImage.getImages().add(image);
            }
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
        List<DayImageDto> dayImageDtos = new ArrayList<>();
        for (Image storedImage : images) {
            URL url = amazonS3.getUrl(bucket, storedImage.getStoredFileName());
            String timeStatus = storedImage.getTimeStatus().getCode();
            dayImageDtos.add(new DayImageDto(url, timeStatus));
        }
        return dayImageDtos;


    }


}
