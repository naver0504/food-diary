package com.fooddiary.api.service;


import com.amazonaws.services.s3.AmazonS3;
import com.fooddiary.api.FileStorageService;
import com.fooddiary.api.common.utils.ImageUtils;
import com.fooddiary.api.dto.request.SaveImageRequestDTO;
import com.fooddiary.api.dto.response.ThumbNailImagesDTO;
import com.fooddiary.api.dto.response.StatusResponseDTO;
import com.fooddiary.api.dto.response.TimeLineResponseDTO;
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

import static com.fooddiary.api.dto.response.StatusResponseDTO.*;

@RequiredArgsConstructor
@Service
@Slf4j
public class DayImageService {

    private final DayImageRepository dayImageRepository;
    private final ImageService imageService;
    private final FileStorageService fileStorageService;
    private final ImageUtils imageUtils;
    private final AmazonS3 amazonS3;

    @Value("${cloud.aws.s3.dir}")
    private String basePath;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    @Transactional
    public StatusResponseDTO saveImage(final List<MultipartFile> files, final SaveImageRequestDTO saveImageRequestDTO, final User user) {

        final LocalDateTime dateTime = saveImageRequestDTO.getLocalDateTime();


        final int year = dateTime.getYear();
        final int month = dateTime.getMonthValue();
        final int day = dateTime.getDayOfMonth();
        final Double longitude = saveImageRequestDTO.getLongitude();
        final Double latitude = saveImageRequestDTO.getLatitude();


        final DayImage dayImage = dayImageRepository.findByYearAndMonthAndDay(year, month, day, user.getId());
        final List<Image> images;

        try {
            images = imageService.storeImage(files, dateTime, user, longitude, latitude, basePath);
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
            newDayImage.updateThumbNailImageName(imageUtils.createThumbnailName(files.get(0), user, amazonS3, bucket, basePath));


        } else {
            /**
             * 변경 감지로 알아서 update 쿼리
             */
            dayImage.setImages(images);
            final String originalThumbnailPath = dayImage.getThumbNailImagePath();
            final String dirPath = ImageUtils.getDirPath(basePath, user);

            fileStorageService.deleteImage(dirPath + originalThumbnailPath);
            dayImage.updateThumbNailImageName(imageUtils.createThumbnailName(files.get(0), user, amazonS3, bucket, basePath));

        }


        return StatusResponseDTO.builder()
                .status(Status.SUCCESS)
                .build();

    }

    public List<ThumbNailImagesDTO> getThumbNailImages(final int year, final int month, final User user)  {
        final List<DayImage> dayImages = dayImageRepository.findByYearAndMonth(year, month, user.getId());
        final List<ThumbNailImagesDTO> dayImagesDTOS = new ArrayList<>();
        final String dirPath = ImageUtils.getDirPath(basePath, user);
        for (DayImage dayImage : dayImages) {
            byte[] bytes;
            try {
                bytes = fileStorageService.getObject(dirPath + dayImage.getThumbNailImagePath());
            } catch (IOException e) {
                log.error("IOException ", e);
                throw new RuntimeException(e);
            }
            final Time time = dayImage.getTime();
            dayImagesDTOS.add(
                    ThumbNailImagesDTO.builder()
                            .id(dayImage.getId())
                            .time(time)
                            .bytes(bytes)
                            .build()
            );

        }
        return dayImagesDTOS;

    }

    @Transactional(readOnly = true)
    public List<TimeLineResponseDTO> getTimeLine(final int year, final int month, final User user) {
        final List<DayImage> dayImages = dayImageRepository.findDayImageByYearAndMonthOrderByDayDesc(year, month, user.getId());
        final String dirPath = ImageUtils.getDirPath(basePath, user);
        final List<TimeLineResponseDTO> timeLineResponseDTOS = new ArrayList<>();


        for (DayImage dayImage : dayImages) {
            final int day = dayImage.getTime().getDay();
            final List<TimeLineResponseDTO.ImageResponseDTO> imageResponseDTOS = new ArrayList<>();
            for (Image image : dayImage.getImages()) {
                final byte[] bytes;
                try {
                    bytes = fileStorageService.getObject(dirPath + image.getStoredFileName());

                } catch (IOException e) {
                    throw new RuntimeException(e);
                }

                final TimeLineResponseDTO.ImageResponseDTO imageResponseDTO = TimeLineResponseDTO.ImageResponseDTO
                        .TimeLineImageResponse(image.getId(), bytes);
                imageResponseDTOS.add(imageResponseDTO);
            }

            final int dayOfWeek = LocalDateTime.of(year, month, day, 0, 0)
                    .getDayOfWeek().getValue();

            timeLineResponseDTOS.add(TimeLineResponseDTO.TimeLineResponse(dayImage, imageResponseDTOS, dayOfWeek));


        }

        return timeLineResponseDTOS;
    }



}
