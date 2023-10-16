package com.fooddiary.api.service;


import com.amazonaws.services.s3.AmazonS3;
import com.fooddiary.api.FileStorageService;
import com.fooddiary.api.common.utils.ImageUtils;
import com.fooddiary.api.dto.response.diary.HomeResponseDTO;
import com.fooddiary.api.entity.image.DayImage;
import com.fooddiary.api.entity.diary.Time;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.ImageQuerydslRepository;
import com.fooddiary.api.repository.diary.DiaryQuerydslRepository;
import com.fooddiary.api.repository.diary.DiaryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RequiredArgsConstructor
@Service
@Slf4j
public class DayImageService {

    //private final DayImageRepository dayImageRepository;
    private final ImageService imageService;
    private final FileStorageService fileStorageService;
    private final ImageUtils imageUtils;
    private final AmazonS3 amazonS3;
    //private final DayImageQuerydslRepository dayImageQuerydslRepository;
    private final DiaryRepository diaryRepository;
    private final DiaryQuerydslRepository diaryQuerydslRepository;
    private final ImageQuerydslRepository imageQuerydslRepository;

    @Value("${cloud.aws.s3.dir}")
    private String basePath;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;


    /*
    @Transactional
    public StatusResponseDTO saveImage(final List<MultipartFile> files, final SaveImageRequestDTO saveImageRequestDTO, final User user) {

        final LocalDateTime dateTime = saveImageRequestDTO.getCreateTime();
        final int year = dateTime.getYear();
        final int month = dateTime.getMonthValue();
        final int day = dateTime.getDayOfMonth();
        final Double longitude = null;// saveImageRequestDTO.getLongitude();
        final Double latitude =  null;//saveImageRequestDTO.getLatitude();


        final int todayDiaryCount = diaryRepository.getByYearAndMonthAndDayCount(year, month, day, user.getId());

        if (todayDiaryCount >= 10) {
            throw new BizException("register only 10 per day");
        }
        final List<Image> images;

        images = imageService.storeImage(files, user, saveImageRequestDTO);

        /***
         * 해당 날짜에 사진들이 있는 지 확인
         * 있다면 사진들을 추가하고 첫 번째 사진 썸네일로
         * 없다면 해당 날짜 이미지 엔티티 생성 후 사진 썸네일 설정
         */

         //   final DayImage newDayImage = DayImage.createDayImage(images, dateTime, user);
         //   dayImageRepository.save(newDayImage);
          //  newDayImage.updateThumbNailImageName(imageUtils.createThumbnailImage(files.get(0), user, amazonS3, bucket, basePath));

        /*
        else {
            dayImage.setImages(images);
            final String originalThumbnailPath = dayImage.getThumbNailImagePath();
            final String dirPath = ImageUtils.getDirPath(basePath, user);

            fileStorageService.deleteImage(dirPath + originalThumbnailPath);
            dayImage.updateThumbNailImageName(imageUtils.createThumbnailImage(files.get(0), user, amazonS3, bucket, basePath));

        }



        return StatusResponseDTO.builder()
                .status(Status.SUCCESS)
                .build();

    }*/


    public List<HomeResponseDTO> getThumbNailImages(final int year, final int month, final User user)  {
        final List<DayImage> dayImages = null; //dayImageRepository.findByYearAndMonth(year, month, user.getId()); todo
        final List<HomeResponseDTO> dayImagesDTOS = new ArrayList<>();
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
                    HomeResponseDTO.builder()
                            .id(dayImage.getId())
                            .time(time)
                            .bytes(bytes)
                            .build()
            );

        }
        return dayImagesDTOS;

    }

    /*
    public List<TimeLineResponseDTO> getTimeLine(final int year, final int month, final int startDay, final User user) {
        final List<DayImage> dayImages = null; // dayImageQuerydslRepository.getTimeLineDayImage(year, month, startDay, user.getId()); todo
        final String dirPath = ImageUtils.getDirPath(basePath, user);
        final List<TimeLineResponseDTO> timeLineResponseDTOS = new ArrayList<>();


        for (DayImage dayImage : dayImages) {
            final int day = dayImage.getTime().getDay();
            final List<TimeLineResponseDTO.ImageResponseDTO> imageResponseDTOS = new ArrayList<>();
            final List<Image> images = imageQuerydslRepository.findByDayImageId(dayImage.getId());

            for (Image image : images) {
                    final byte[] bytes;
                    try {
                        bytes = fileStorageService.getObject(dirPath + image.getStoredFileName());

                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }

                    final TimeLineResponseDTO.ImageResponseDTO imageResponseDTO = TimeLineResponseDTO.ImageResponseDTO
                            .createImageResponseDTO(image.getId(), bytes);
                    imageResponseDTOS.add(imageResponseDTO);
            }

            final int dayOfWeek = LocalDateTime.of(year, month, day, 0, 0)
                    .getDayOfWeek().getValue();

            timeLineResponseDTOS.add(TimeLineResponseDTO.TimeLineResponse(dayImage, imageResponseDTOS, dayOfWeek));
        }

        return timeLineResponseDTOS;
    }*/

}
