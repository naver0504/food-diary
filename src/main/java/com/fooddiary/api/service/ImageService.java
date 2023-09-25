package com.fooddiary.api.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.fooddiary.api.FileStorageService;
import com.fooddiary.api.common.utils.ImageUtils;
import com.fooddiary.api.dto.request.UpdateImageDetailDTO;
import com.fooddiary.api.dto.response.*;
import com.fooddiary.api.entity.image.Image;
import com.fooddiary.api.entity.image.Time;
import com.fooddiary.api.entity.image.TimeStatus;
import com.fooddiary.api.entity.tag.Tag;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.DayImageQuerydslRepository;
import com.fooddiary.api.repository.DayImageRepository;
import com.fooddiary.api.repository.ImageQuerydslRepository;
import com.fooddiary.api.repository.ImageRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final DayImageRepository dayImageRepository;
    private final AmazonS3 amazonS3;
    private final FileStorageService fileStorageService;
    private final TagService tagService;
    private final DayImageQuerydslRepository dayImageQuerydslRepository;
    private final ImageQuerydslRepository imageQuerydslRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.dir}")
    private String basePath;

    public List<Image> storeImage(final List<MultipartFile> files, final LocalDateTime localDateTime, final User user, final Double longitude, final Double latitude,final String basePath) throws IOException {

        final List<Image> images = new ArrayList<>();
        Image firstImage = null;

        for (int i = 0; i<files.size(); i++) {
            final MultipartFile file = files.get(i);

            //파일 명 겹치면 안되므로 UUID + '-' + 원래 파일 이름으로 저장

            final String storeFilename = ImageUtils.createImageName(file.getOriginalFilename());
            final Image image = Image.createImage(localDateTime, storeFilename, longitude, latitude, user);
            if (i == 0) {
                firstImage = image;
            } else {
                firstImage.addChildImage(image);
            }
            final int userId = user.getId();


            //S3에 저장하는 로직
            try {
                ObjectMetadata metadata = new ObjectMetadata();

                final String dirPath = ImageUtils.getDirPath(basePath, user);
                int count = dayImageRepository.getDayImageCount(userId);
                if(count == 0) {
                    amazonS3.putObject(bucket, dirPath, new ByteArrayInputStream(new byte[0]), new ObjectMetadata());
                }

                amazonS3.putObject(bucket, dirPath+storeFilename, file.getInputStream(), metadata);
            } catch (AmazonServiceException e) {
                log.error("AmazonServiceException ", e);
                throw new RuntimeException(e.getMessage());
            } catch (SdkClientException e) {
                log.error("SdkClientException ", e);
                throw new RuntimeException(e.getMessage());
            } catch (IOException e) {
                log.error("IOException ", e);
                throw new RuntimeException(e.getMessage());
            }


            final Image saveImage = imageRepository.save(image);
            images.add(saveImage);
        }
        return images;

    }

    public ShowImageOfDayDTO getImages(int year, int month, int day, final User user) {
        final List<Image> images = imageQuerydslRepository.findByYearAndMonthAndDay(year, month, day, user.getId());
        final List<ShowImageOfDayDTO.ImageDTO> ImageDTOS = new ArrayList<>();
        final String dirPath = ImageUtils.getDirPath(basePath, user);

        for (Image storedImage : images) {
            byte[] bytes;

            try {
                bytes = fileStorageService.getObject(dirPath + storedImage.getStoredFileName());
            } catch (IOException e) {
                log.error("IOException ", e);
                throw new RuntimeException(e);
            }
            final TimeStatus timeStatus = storedImage.getTimeStatus();

            final String time = storedImage.getTimeStatus().getCode();
            ImageDTOS.add(
                    ShowImageOfDayDTO.ImageDTO.builder()
                            .bytes(bytes)
                            .timeStatus(timeStatus)
                            .time(time)
                            .tags(Tag.toStringList(storedImage.getTags()))
                            .id(storedImage.getId())
                            .build()
            );
        }

        ImageDTOS.sort((o1, o2) ->
                o1.getTimeStatus().compareTo(o2.getTimeStatus()));
        ShowImageOfDayDTO.ShowImageOfDayDTOBuilder showImageOfDayDTOBuilder = ShowImageOfDayDTO.builder()
                .images(ImageDTOS);
        final Map<String, Time> beforeAndAfterDay = dayImageQuerydslRepository.getBeforeAndAfterTime(year, month, day, user.getId());


        if(beforeAndAfterDay.containsKey("before")) {
            final Time before = beforeAndAfterDay.get("before");
            showImageOfDayDTOBuilder.beforeTime(TimeDetailDTO.of(before));
        }
        if(beforeAndAfterDay.containsKey("after")) {
            final Time after = beforeAndAfterDay.get("after");
            showImageOfDayDTOBuilder.afterTime(TimeDetailDTO.of(after));
        }
        showImageOfDayDTOBuilder.todayTime(TimeDetailDTO.of(new Time(Time.getDateTime(year, month, day))));

        return showImageOfDayDTOBuilder.build();

    }

    public ImageDetailResponseDTO getImageDetail(final Integer imageId, final User user) {
        final Image image = imageRepository.findByIdWithTag(imageId, user.getId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이미지입니다."));
        final List<TimeLineResponseDTO.ImageResponseDTO> imageResponseDTOS = new ArrayList<>();

        final List<Image> children = imageRepository.findByParentImageId(imageId);


        try {
            byte[] bytes = fileStorageService.getObject(ImageUtils.getDirPath(basePath, user) + image.getStoredFileName());
            imageResponseDTOS.add(TimeLineResponseDTO.ImageResponseDTO.builder()
                    .bytes(bytes)
                    .id(image.getId())
                    .build());

            for(Image child : children){
                bytes = fileStorageService.getObject(ImageUtils.getDirPath(basePath, user) + child.getStoredFileName());
                imageResponseDTOS.add(TimeLineResponseDTO.ImageResponseDTO.builder()
                        .bytes(bytes)
                        .id(child.getId())
                        .build());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        final List<String> tags = image.getTags()
                .stream().map(tag -> tag.getTagName()).collect(Collectors.toList());

        final Time time = dayImageQuerydslRepository.getTime(imageId);

        return ImageDetailResponseDTO.builder()
                .memo(image.getMemo())
                .TimeStatus(image.getTimeStatus().getCode())
                .tags(tags)
                .timeDetail(TimeDetailDTO.of(time))
                .images(imageResponseDTOS)
                .build();
    }

    @Transactional
    public StatusResponseDTO updateImageDetail(final Integer imageId, final User user, final UpdateImageDetailDTO updateImageDetailDTO) {
        final Image image = imageRepository.findByIdWithTag(imageId, user.getId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이미지입니다."));
        final List<String> tags = updateImageDetailDTO.getTags();
        final List<Tag> originalTags = image.getTags();
        final List<Integer> deleteTagIds = new ArrayList<>();
        final List<Tag> addTags = new ArrayList<>();
        image.update(updateImageDetailDTO.getMemo(), TimeStatus.getTimeStatusByCode(updateImageDetailDTO.getTimeStatus()));

        tags.forEach(tag -> {
            if (originalTags.stream().noneMatch(originalTag -> originalTag.getTagName().equals(tag))) {
                addTags.add(Tag.builder()
                        .tagName(tag)
                        .image(image)
                        .user(user)
                        .build()
                );
            }
        });

        originalTags.forEach(originalTag -> {
            if (tags.stream().noneMatch(tag -> tag.equals(originalTag.getTagName()))) {
                deleteTagIds.add(originalTag.getId());
            }
        });

        image.setTag(addTags);

        if(addTags.size() != 0) {
            tagService.saveAll(addTags);
        }
        if(deleteTagIds.size() != 0) {
            tagService.deleteAllById(deleteTagIds);
        }
        return StatusResponseDTO.builder()
                .status(StatusResponseDTO.Status.SUCCESS)
                .build();

    }


}
