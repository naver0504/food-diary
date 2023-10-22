package com.fooddiary.api.service;

import com.amazonaws.SdkClientException;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.model.ObjectMetadata;
import com.amazonaws.services.s3.model.PutObjectRequest;
import com.fooddiary.api.FileStorageService;
import com.fooddiary.api.common.exception.BizException;
import com.fooddiary.api.common.util.ImageUtils;
import com.fooddiary.api.dto.request.diary.PlaceInfoDTO;
import com.fooddiary.api.dto.response.*;
import com.fooddiary.api.dto.response.image.ImageResponseDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.diary.Image;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.ImageQuerydslRepository;
import com.fooddiary.api.repository.ImageRepository;
import com.fooddiary.api.repository.diary.DiaryRepository;

import com.fooddiary.api.service.diary.TagService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.time.LocalDateTime;
import java.util.*;


@Service
@RequiredArgsConstructor
@Slf4j
public class ImageService {

    private final ImageRepository imageRepository;
    private final DiaryRepository diaryRepository;
    private final AmazonS3 amazonS3;
    private final FileStorageService fileStorageService;
    private final TagService tagService;
    private final ImageQuerydslRepository imageQuerydslRepository;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    @Value("${cloud.aws.s3.dir}")
    private String basePath;

    public List<Image> storeImage(final int diaryId, final List<MultipartFile> files, final User user)  {

        final List<Image> images = new ArrayList<>();
        final String dirPath = ImageUtils.getDirPath(basePath, user);

        if (!amazonS3.doesObjectExist(bucket, dirPath)) {
            amazonS3.putObject(bucket, dirPath, new ByteArrayInputStream(new byte[0]), new ObjectMetadata());
        }

        Diary diary = diaryRepository.findById(diaryId).orElseThrow(() -> new BizException("invalid diaryId"));

        for (final MultipartFile file : files) {
            final String storeFilename = ImageUtils.createImageName(file.getOriginalFilename());
            final Image image = Image.createImage(diary, storeFilename);

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
            image.setUpdateAt(LocalDateTime.now());
            final Image saveImage = imageRepository.save(image);
            images.add(saveImage);
        }
        return images;
    }

    public void storeImage(final Diary diary, final List<MultipartFile> files, final User user)  {

        final String dirPath = ImageUtils.getDirPath(basePath, user);

        if (!amazonS3.doesObjectExist(bucket, dirPath)) {
            amazonS3.putObject(bucket, dirPath, new ByteArrayInputStream(new byte[0]), new ObjectMetadata());
        }

        for (final MultipartFile file : files) {
            final String storeFilename = ImageUtils.createImageName(file.getOriginalFilename());
            final Image image = Image.createImage(diary, storeFilename);

            final ObjectMetadata metadata = new ObjectMetadata();
            metadata.setContentLength(file.getSize());
            metadata.setContentType(file.getContentType());

            try (ByteArrayOutputStream thumbnailOutputStream = ImageUtils.createThumbnailImage(files.get(0), user, amazonS3, bucket, basePath);
                 ByteArrayInputStream thumbnailInputStream = new ByteArrayInputStream(thumbnailOutputStream.toByteArray())) {
                inputIntoFileStorage(dirPath, storeFilename, file.getInputStream());

                final String storeThumbnailFilename = "t_" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
                inputIntoFileStorage(dirPath, storeThumbnailFilename, thumbnailInputStream);
                image.setThumbnailFileName(storeThumbnailFilename);
            } catch (IOException e) {
                log.error("storeImage error, user id;" + user.getId() + ", diary;" + diary.getId());
                throw new RuntimeException(e);
            }
            image.setUpdateAt(LocalDateTime.now());
            imageRepository.save(image);
        }
    }

    public void inputIntoFileStorage(final String dirPath, final String storeFilename, final InputStream inputStream) throws IOException {
        try (inputStream) {
            final ObjectMetadata metadata = new ObjectMetadata();
            PutObjectRequest putObjectRequest = new PutObjectRequest(bucket, dirPath + storeFilename, inputStream, metadata);
            amazonS3.putObject(putObjectRequest);
        } catch (SdkClientException e) {
            log.error("aws exception", e);
            throw new IOException(e.getMessage());
        }
    }

    public List<ImageResponseDTO> getImages(final List<Image> images, final User user) {
        List<ImageResponseDTO> imageResponseDTOList = new LinkedList<>();
        final String dirPath = ImageUtils.getDirPath(basePath, user);
        for (Image image : images) {
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

    public ImageResponseDTO getImage (final Image image, final User user) {
        final String dirPath = ImageUtils.getDirPath(basePath, user);
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
        return imageResponseDTO;
    }

    public StatusResponseDTO updateImage(final int imageId, final MultipartFile file, final User user) {
        final Image image = imageRepository.findByImageIdAndUserId(imageId)
                .orElseThrow(() -> new RuntimeException("존재하지 않는 이미지입니다."));
        final String storeFilename = ImageUtils.createImageName(file.getOriginalFilename());
        final String dirPath = ImageUtils.getDirPath(basePath, user);

        try (ByteArrayOutputStream thumbnailOutputStream = ImageUtils.createThumbnailImage(file, user, amazonS3, bucket, basePath);
             ByteArrayInputStream thumbnailInputStream = new ByteArrayInputStream(thumbnailOutputStream.toByteArray())) {
            fileStorageService.deleteImage(dirPath + image.getStoredFileName());
            fileStorageService.deleteImage(dirPath + image.getThumbnailFileName());

            inputIntoFileStorage(dirPath, storeFilename, file.getInputStream());
            image.setStoredFileName(storeFilename);

            final String storeThumbnailFilename = "t_" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            inputIntoFileStorage(dirPath, storeThumbnailFilename, thumbnailInputStream);
            image.setThumbnailFileName(storeThumbnailFilename);
            image.setUpdateAt(LocalDateTime.now());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return StatusResponseDTO.builder()
                .status(StatusResponseDTO.Status.SUCCESS)
                .build();
    }

    public void updateImage(final Image image, final MultipartFile file, final User user) {

        final String storeFilename = ImageUtils.createImageName(file.getOriginalFilename());
        final String dirPath = ImageUtils.getDirPath(basePath, user);

        try (ByteArrayOutputStream thumbnailOutputStream = ImageUtils.createThumbnailImage(file, user, amazonS3, bucket, basePath);
             ByteArrayInputStream thumbnailInputStream = new ByteArrayInputStream(thumbnailOutputStream.toByteArray())) {
            fileStorageService.deleteImage(dirPath + image.getStoredFileName());
            fileStorageService.deleteImage(dirPath + image.getThumbnailFileName());

            inputIntoFileStorage(dirPath, storeFilename, file.getInputStream());
            image.setStoredFileName(storeFilename);
            final String storeThumbnailFilename = "t_" + UUID.randomUUID().toString() + "_" + file.getOriginalFilename();
            inputIntoFileStorage(dirPath, storeThumbnailFilename, thumbnailInputStream);
            image.setThumbnailFileName(storeThumbnailFilename);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        image.setUpdateAt(LocalDateTime.now());
        // image.setGeography(newDiaryRequestDTO.getLongitude(), newDiaryRequestDTO.getLatitude());
        imageRepository.save(image);
    }

}
