package com.fooddiary.api.service;

import com.amazonaws.AmazonServiceException;
import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fooddiary.api.FileStorageService;
import com.fooddiary.api.common.utils.ImageUtils;
import com.fooddiary.api.dto.request.SaveImageRequestDTO;
import com.fooddiary.api.dto.response.*;
import com.fooddiary.api.dto.response.image.ImageResponseDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.image.Image;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.DayImageQuerydslRepository;
import com.fooddiary.api.repository.DayImageRepository;
import com.fooddiary.api.repository.ImageQuerydslRepository;
import com.fooddiary.api.repository.ImageRepository;
import com.fooddiary.api.repository.diary.DiaryRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final DayImageRepository dayImageRepository;
    private final DiaryRepository diaryRepository;
    private final AmazonS3 amazonS3;
    private final FileStorageService fileStorageService;
    private final TagService tagService;
    private final DayImageQuerydslRepository dayImageQuerydslRepository;
    private final ImageQuerydslRepository imageQuerydslRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.dir}")
    private String basePath;

    public List<Image> storeImage(final int diaryId, final List<MultipartFile> files, final User user, final SaveImageRequestDTO saveImageRequestDTO)  {

        final List<Image> images = new ArrayList<>();
        final String dirPath = ImageUtils.getDirPath(basePath, user);

        if (!amazonS3.doesObjectExist(bucket, dirPath)) {
            amazonS3.putObject(bucket, dirPath, new ByteArrayInputStream(new byte[0]), new ObjectMetadata());
        }

        Diary diary = diaryRepository.findById(diaryId).get();

        for (int i = 0; i<files.size(); i++) {
            final MultipartFile file = files.get(i);
            final String storeFilename = ImageUtils.createImageName(file.getOriginalFilename());
            final Image image = Image.createImage(diary, storeFilename, saveImageRequestDTO);

            final ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            try {
                inputIntoFileStorage(dirPath, storeFilename, file.getInputStream());
                ByteArrayOutputStream thumbnailOutputStream = ImageUtils.createThumbnailImage(files.get(0), user, amazonS3, bucket, basePath);
                final ByteArrayInputStream thumbnailInputStream = new ByteArrayInputStream(thumbnailOutputStream.toByteArray());
                final String storeThumbnailFilename = "t_" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                inputIntoFileStorage(dirPath, storeThumbnailFilename, thumbnailInputStream);
                image.setThumbnailFileName(storeThumbnailFilename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }

            final Image saveImage = imageRepository.save(image);
            images.add(saveImage);
        }
        return images;
    }

    public void storeImage(final Diary diary, final List<MultipartFile> files, final User user, final SaveImageRequestDTO saveImageRequestDTO)  {

        final String dirPath = ImageUtils.getDirPath(basePath, user);

        if (!amazonS3.doesObjectExist(bucket, dirPath)) {
            amazonS3.putObject(bucket, dirPath, new ByteArrayInputStream(new byte[0]), new ObjectMetadata());
        }

        for (int i = 0; i<files.size(); i++) {
            final MultipartFile file = files.get(i);
            final String storeFilename = ImageUtils.createImageName(file.getOriginalFilename());
            final Image image = Image.createImage(diary, storeFilename, saveImageRequestDTO);

            final ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            try {
                inputIntoFileStorage(dirPath, storeFilename, file.getInputStream());
                ByteArrayOutputStream thumbnailOutputStream = ImageUtils.createThumbnailImage(files.get(0), user, amazonS3, bucket, basePath);
                final ByteArrayInputStream thumbnailInputStream = new ByteArrayInputStream(thumbnailOutputStream.toByteArray());
                final String storeThumbnailFilename = "t_" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                inputIntoFileStorage(dirPath, storeThumbnailFilename, thumbnailInputStream);
                image.setThumbnailFileName(storeThumbnailFilename);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            imageRepository.save(image);
        }
    }

    public void inputIntoFileStorage(final String dirPath, final String storeFilename, final InputStream inputStream) {
        try {
            final ObjectMetadata metadata = new ObjectMetadata();
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, dirPath + storeFilename, inputStream, metadata);
            amazonS3.putObject(putObjectRequest);
        } catch (AmazonServiceException e) {
            log.error("AmazonServiceException ", e);
            throw new RuntimeException(e.getMessage());
        } catch (SdkClientException e) {
            log.error("SdkClientException ", e);
            throw new RuntimeException(e.getMessage());
        }
    }

    public List<ImageResponseDTO> getImages(final Diary diary, final User user) {
        List<ImageResponseDTO> imageResponseDTOList = new LinkedList<>();
        final String dirPath = ImageUtils.getDirPath(basePath, user);
        for (Image image : diary.getImages()) {
            byte[] bytes;
            try {
                bytes = fileStorageService.getObject(dirPath + image.getStoredFileName());
            } catch (IOException e) {
                log.error("IOException ", e);
                throw new RuntimeException(e);
            }
            ImageResponseDTO imageResponseDTO = new ImageResponseDTO();
            imageResponseDTO.setImageId(image.getId());
            imageResponseDTO.setBytes(bytes);
            imageResponseDTOList.add(imageResponseDTO);
        }
        return imageResponseDTOList;
    }

    /*
    public ShowImageOfDayDTO getImages(final int year, final int month, final int day, final User user) {
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
            // final TimeStatus timeStatus = storedImage.getTimeStatus();

            // final String time = storedImage.getTimeStatus().getCode();
            ImageDTOS.add(
                    ShowImageOfDayDTO.ImageDTO.builder()
                            .bytes(bytes)
                  //           .timeStatus(timeStatus)
                  //           .time(time)
                            .id(storedImage.getId())
                            .build()
            );
        }

       // ImageDTOS.sort((o1, o2) ->
       //         o1.getTimeStatus().compareTo(o2.getTimeStatus()));
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

    public ImageDetailResponseDTO getImageDetail(final int imageId, final User user) {
        final Image image = imageRepository.findByIdWithTag(imageId, user.getId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이미지입니다."));
        final List<TimeLineResponseDTO.ImageResponseDTO> imageResponseDTOS = new ArrayList<>();

        final List<Image> children = imageRepository.findByParentImageId(imageId);


        try {
            byte[] bytes = fileStorageService.getObject(ImageUtils.getDirPath(basePath, user) + image.getStoredFileName());
            imageResponseDTOS.add(TimeLineResponseDTO.ImageResponseDTO.builder()
                    .bytes(bytes)
                    .imageId(image.getId())
                    .build());

            for(Image child : children){
                bytes = fileStorageService.getObject(ImageUtils.getDirPath(basePath, user) + child.getStoredFileName());
                imageResponseDTOS.add(TimeLineResponseDTO.ImageResponseDTO.builder()
                        .bytes(bytes)
                        .imageId(child.getId())
                        .build());
            }
        } catch (IOException e) {
            throw new RuntimeException(e);
        }


        final Time time = dayImageQuerydslRepository.getTime(imageId);

        return ImageDetailResponseDTO.builder()
                .timeStatus(image.getDiaryTime().getCode())
                .timeDetail(TimeDetailDTO.of(time))
                .images(imageResponseDTOS)
                .build();
    }

    @Transactional
    public StatusResponseDTO uploadImageFile(final List<MultipartFile> files, final int imageId, final User user) {


        for (int i = 0; i<files.size(); i++) {
            final MultipartFile file = files.get(i);

            //파일 명 겹치면 안되므로 UUID + '-' + 원래 파일 이름으로 저장
            final String storeFilename = ImageUtils.createImageName(file.getOriginalFilename());
            Image parentImage = imageRepository.findParentImageByImageIdAndUserId(imageId, user.getId())
                    .orElseThrow(() -> new RuntimeException("존재하지 않는 이미지입니다."));
            Image newImage = Image.createImage(parentImage, storeFilename, user);
            // parentImage.addChildImage(newImage);

            //S3에 저장하는 로직
            try {
                ObjectMetadata metadata = new ObjectMetadata();
                metadata.setContentLength(file.getSize());
                metadata.setContentType(file.getContentType());
                final String dirPath = ImageUtils.getDirPath(basePath, user);
                PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, dirPath+storeFilename, file.getInputStream(), metadata);
                amazonS3.putObject(putObjectRequest);
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


            imageRepository.save(newImage);
        }


        return StatusResponseDTO.builder()
                .status(StatusResponseDTO.Status.SUCCESS)
                .build();
    }

    public List<TimeLineResponseDTO.ImageResponseDTO> getTimeLineImagesWithStartImageId(final int year, final int month, final int day,
                                                                                        final int startImageId, final User user) {

        List<Image> images = imageQuerydslRepository.findByYearAndMonthAndDayAndStartId(year, month, day, startImageId, user.getId());
        List<TimeLineResponseDTO.ImageResponseDTO> imageResponseDTOS = new ArrayList<>();
        for (Image image : images) {
            byte[] bytes;
            try {
                bytes = fileStorageService.getObject(ImageUtils.getDirPath(basePath, user) + image.getStoredFileName());
            } catch (IOException e) {
                log.error("IOException ", e);
                throw new RuntimeException(e);
            }
            imageResponseDTOS.add(TimeLineResponseDTO.ImageResponseDTO.createImageResponseDTO(image.getId(), bytes));
        }

        return imageResponseDTOS;

    }

    @Transactional
    public StatusResponseDTO updateImageDetail(final int parentImageId, final User user, final UpdateImageDetailDTO updateImageDetailDTO) {
        final Image image = imageRepository.findByIdWithTag(parentImageId, user.getId())
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이미지입니다."));
        final List<String> diaryTags = updateImageDetailDTO.getDiaryTags();
        final List<Integer> deleteTagIds = new ArrayList<>();
        final List<DiaryTag> addTags = new ArrayList<>();

        if(addTags.size() != 0) {
            tagService.saveAll(addTags);
        }
        if(deleteTagIds.size() != 0) {
            tagService.deleteAllById(deleteTagIds);
        }

        if (!image.getDiaryTime().getCode().equals(updateImageDetailDTO.getTimeStatus())) {
            imageQuerydslRepository.updateTimeStatus(image.getId(), DiaryTime.getTimeStatusByCode(updateImageDetailDTO.getTimeStatus()));
        }

        return StatusResponseDTO.builder()
                .status(StatusResponseDTO.Status.SUCCESS)
                .build();
    }

*/
    @Transactional
    public StatusResponseDTO updateImage(final int imageId, final MultipartFile file, final User user) {
        final Image image = imageRepository.findByImageIdAndUserId(imageId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이미지입니다."));
        final String storeFilename = ImageUtils.createImageName(file.getOriginalFilename());
        final String dirPath = ImageUtils.getDirPath(basePath, user);

        try {
            fileStorageService.deleteImage(dirPath + image.getStoredFileName());
            fileStorageService.deleteImage(dirPath + image.getThumbnailFileName());

            image.setStoredFileName(storeFilename);
            inputIntoFileStorage(dirPath, storeFilename, file.getInputStream());
            ByteArrayOutputStream thumbnailOutputStream = ImageUtils.createThumbnailImage(file, user, amazonS3, bucket, basePath);
            final ByteArrayInputStream thumbnailInputStream = new ByteArrayInputStream(thumbnailOutputStream.toByteArray());
            final String storeThumbnailFilename = "t_" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            inputIntoFileStorage(dirPath, storeThumbnailFilename, thumbnailInputStream);
            image.setThumbnailFileName(storeThumbnailFilename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return StatusResponseDTO.builder()
                .status(StatusResponseDTO.Status.SUCCESS)
                .build();
    }

}
