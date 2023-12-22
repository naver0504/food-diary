package com.fooddiary.api.service.diary;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import com.amazonaws.services.s3.AmazonS3;
import com.fooddiary.api.FileStorageService;
import com.fooddiary.api.common.util.ImageUtils;
import com.fooddiary.api.dto.request.diary.DiaryMemoRequestDTO;
import com.fooddiary.api.dto.response.diary.*;
import com.fooddiary.api.entity.diary.*;
import com.fooddiary.api.entity.diary.DiaryTime;
import com.fooddiary.api.repository.ImageRepository;
import com.fooddiary.api.repository.diary.*;
import com.fooddiary.api.service.ImageService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.fooddiary.api.common.exception.BizException;
import com.fooddiary.api.dto.request.diary.PlaceInfoDTO;
import com.fooddiary.api.entity.user.User;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryTransactionService diaryTransactionService;
    private final DiaryRepository diaryRepository;
    private final DiaryQuerydslRepository diaryQuerydslRepository;
    private final DiaryTagRepository diaryTagRepository;
    private final ImageRepository imageRepository;
    private final TagRepository tagRepository;
    private final FileStorageService fileStorageService;
    private final ImageService imageService;
    private final AmazonS3 amazonS3;
    @Value("${cloud.aws.s3.dir}")
    private String basePath;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public void createDiary(final List<MultipartFile> files, final LocalDate createDate, final PlaceInfoDTO placeInfoDTO,
                            final User user) {
        final LocalDateTime startDate = createDate.atStartOfDay();
        final LocalDateTime endDate = createDate.plusDays(1L).atStartOfDay().minusSeconds(1L).withNano(999999000);
        final int todayDiaryCount = diaryRepository.getByYearAndMonthAndDayCount(startDate, endDate,
                                                                                 user.getId());

        if (todayDiaryCount >= 10) {
            throw new BizException("register only 10 per day");
        }

        final Diary newDiary = Diary.createDiary(createDate, user, DiaryTime.ETC, placeInfoDTO);
        diaryRepository.save(newDiary);
        imageService.storeImage(newDiary, files, user);
    }

    public void addImages(final long diaryId, final List<MultipartFile> files, final User user) {
        if (diaryRepository.getDiaryImagesCount(diaryId) + files.size() > 5) {
            throw new BizException("we allow max 5 images");
        }
        final Diary diary = diaryRepository.findById(diaryId).orElse(null);
        if (diary == null) {
            throw new BizException("invalid diary id");
        }
        if (!diary.getUser().getId().equals(user.getId())) {
            throw new BizException("no permission user");
        }

        imageService.storeImage(diary, files, user);
        diaryRepository.save(diary);
    }

    public void updateImage(final int imageId, final MultipartFile file, final User user) {
        final Image image = imageRepository.findById(imageId).orElse(null);
        if (image == null) {
            throw new BizException("invalid image id");
        }
        final Diary diary = image.getDiary();

        if (!diary.getUser().getId().equals(user.getId())) {
            throw new BizException("no permission user");
        }
        imageService.updateImage(image.getId(), file, user);
        diary.setUpdateAt(LocalDateTime.now());
        diaryRepository.save(diary);
    }

    public DiaryDetailResponseDTO getDiaryDetail(final long id, final User user) {
        final Diary diary = diaryRepository.findDiaryAndImagesById(id).orElse(null);
        if (diary == null) {
            throw new BizException("invalid diary id");
        }
        DiaryDetailResponseDTO diaryDetailResponseDTO = new DiaryDetailResponseDTO();
        getMemo(diary, diaryDetailResponseDTO);
        diaryDetailResponseDTO.setImages(imageService.getImages(diary.getImages(), user));
        return diaryDetailResponseDTO;
    }

    public DiaryMemoResponseDTO getDiaryMemo(final long id, final User user) {
        final Diary diary = diaryRepository.findDiaryAndImagesById(id).orElse(null);
        if (diary == null) {
            throw new BizException("invalid diary id");
        }
        DiaryMemoResponseDTO diaryMemoResponseDTO = new DiaryMemoResponseDTO();
        getMemo(diary, diaryMemoResponseDTO);
        return diaryMemoResponseDTO;
    }

    private void getMemo(Diary diary, DiaryMemoResponseDTO diaryMemoResponseDTO) {
        diaryMemoResponseDTO.setTags(diary.getDiaryTags().stream().map(tag -> {
            final DiaryDetailResponseDTO.TagResponse tagResponse = new DiaryDetailResponseDTO.TagResponse();
            tagResponse.setName(tag.getTag().getTagName());
            tagResponse.setId(tag.getId());
            return tagResponse;
        }).collect(Collectors.toList()));
        diaryMemoResponseDTO.setDate(diary.getCreateTime().toLocalDate());
        diaryMemoResponseDTO.setMemo(diary.getMemo());
        diaryMemoResponseDTO.setPlace(diary.getPlace());
        if (diary.getGeography() != null) {
            diaryMemoResponseDTO.setLongitude(diary.getGeography().getX());
            diaryMemoResponseDTO.setLatitude(diary.getGeography().getY());
        }
        diaryMemoResponseDTO.setDiaryTime(diary.getDiaryTime().name());
    }

    public void updateMemo(final long diaryId, final DiaryMemoRequestDTO diaryMemoRequestDTO) {
        final Map<String, Tag> deleteTagMap = new HashMap<>(); // 기존에 있었으나 더이상 쓰지 않는 태그
        diaryTransactionService.updateMemo(diaryId, diaryMemoRequestDTO, deleteTagMap);

        deleteTagMap.keySet().forEach(tagName -> {
            final Tag tag = tagRepository.findTagByTagName(tagName);
            try {
                if (tag != null && tag.getDiaryTags().size() < 2) {
                    tagRepository.delete(tag);
                }
            } catch (DataIntegrityViolationException e) {
                log.info("can't delete tag, tag id: {}", tag.getId());
            }
        });
    }

    public void deleteDiary(final long diaryId, final User user) {
        final Diary diary = diaryRepository.findByUserIdAndId(user.getId(), diaryId);
        if (diary == null) {
            throw new BizException("invalid diary id");
        }

        final List<Image> imageList = diary.getImages();
        for (Image image : imageList) {
            amazonS3.deleteObject(bucket, ImageUtils.getDirPath(basePath, user) + image.getStoredFileName());
            amazonS3.deleteObject(bucket, ImageUtils.getDirPath(basePath, user) + image.getThumbnailFileName());
            imageRepository.delete(image);
        }

        final List<DiaryTag> diaryTags = diary.getDiaryTags();
        final List<DiaryTag> deleteDiaryTagList = new ArrayList<>();
        final List<Tag> deletableTagList = new ArrayList<>();
        for (DiaryTag diaryTag : diaryTags) {
            final Tag tag = diaryTag.getTag();
            tag.getDiaryTags().remove(diaryTag);
            deleteDiaryTagList.add(diaryTag);
            if (tag.getDiaryTags().isEmpty()) {
                deletableTagList.add(tag);
            }
        }
        diary.getDiaryTags().removeAll(deleteDiaryTagList);
        diaryTagRepository.deleteAll(deleteDiaryTagList);

        deletableTagList.forEach(tag -> {
            try {
                tagRepository.delete(tag);
            } catch (DataIntegrityViolationException e) {
                log.info("can't delete tag, tag id: {}", tag.getId());
            }
        });

        diaryRepository.delete(diary);
    }

    public List<HomeResponseDTO> getHome(final YearMonth yearMonth, final User user) throws IOException {
        final List<HomeResponseDTO> homeResponseDTOList = new LinkedList<>();
        final LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        final LocalDateTime endDate = yearMonth.atEndOfMonth().plusDays(1L).atStartOfDay().minusSeconds(1L).withNano(999999000);
        final List<Diary> diaryList = diaryRepository.findByYearAndMonth(startDate, endDate, user.getId());
        for (Diary diary : diaryList) {
            if (!diary.getImages().isEmpty()) {
                final Image image = diary.getImages().get(0);
                final HomeResponseDTO homeResponseDTO = new HomeResponseDTO();
                homeResponseDTO.setId(diary.getId());
                homeResponseDTO.setTime(diary.getCreateTime().toLocalDate());
                homeResponseDTO.setBytes(fileStorageService.getObject(ImageUtils.getDirPath(basePath, user) + image.getThumbnailFileName()));
                homeResponseDTOList.add(homeResponseDTO);
            }
        }
        return homeResponseDTOList;
    }

    public HomeDayResponseDTO getHomeDay(final LocalDate date, final User user) {
        final List<HomeDayResponseDTO.HomeDay> homeDayList = new LinkedList<>();
        final LocalDateTime startDate = date.atStartOfDay();
        final LocalDateTime endDate = date.plusDays(1).atStartOfDay().minusSeconds(1L).withNano(999999000);
        final List<Diary> diaryList = diaryRepository.findByYearAndMonthAndDay(startDate, endDate, user.getId());

        for (Diary diary : diaryList) {
            if (!diary.getImages().isEmpty()) {
                final Image image = diary.getImages().get(0);
                HomeDayResponseDTO.HomeDay.HomeDayBuilder homeDayBuilder = HomeDayResponseDTO.HomeDay.builder();
                homeDayBuilder = homeDayBuilder.id(diary.getId())
                        .memo(diary.getMemo())
                        .diaryTime(diary.getDiaryTime())
                        .tags(diary.getDiaryTags().stream().map(diaryTag -> diaryTag.getTag().getTagName()).collect(Collectors.toList()))
                        .place(diary.getPlace());
                if (diary.getGeography() != null) {
                    homeDayBuilder = homeDayBuilder.longitude(diary.getGeography().getX())
                                          .latitude(diary.getGeography().getY());
                }
                homeDayList.add(homeDayBuilder.image(imageService.getImage(image, user)).build());
            }
        }

        final HomeDayResponseDTO homeDayResponseDTO = new HomeDayResponseDTO();
        homeDayResponseDTO.setHomeDayList(homeDayList);

        final Map<String, LocalDateTime> timeMap =  diaryQuerydslRepository.getBeforeAndAfterTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), user.getId());
        if (timeMap.get("before") != null) {
            final LocalDateTime before = timeMap.get("before");
            homeDayResponseDTO.setBeforeDay(LocalDate.of(before.getYear(), before.getMonth(), before.getDayOfMonth()));
        }
        if (timeMap.get("after") != null) {
            final LocalDateTime after = timeMap.get("after");
            homeDayResponseDTO.setAfterDay(LocalDate.of(after.getYear(), after.getMonth(), after.getDayOfMonth()));
        }

        return homeDayResponseDTO;
    }

}
