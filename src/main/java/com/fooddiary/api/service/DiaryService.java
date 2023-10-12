package com.fooddiary.api.service;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Collectors;

import com.fooddiary.api.FileStorageService;
import com.fooddiary.api.common.utils.ImageUtils;
import com.fooddiary.api.dto.request.diary.DiaryMemoRequestDTO;
import com.fooddiary.api.dto.response.ThumbNailImagesDTO;
import com.fooddiary.api.dto.response.diary.DiaryDetailResponseDTO;
import com.fooddiary.api.entity.image.DiaryTime;
import com.fooddiary.api.entity.tag.DiaryTag;
import com.fooddiary.api.entity.tag.Tag;
import com.fooddiary.api.repository.DiaryTagRepository;
import com.fooddiary.api.repository.TagRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fooddiary.api.common.exception.BizException;
import com.fooddiary.api.dto.request.SaveImageRequestDTO;
import com.fooddiary.api.dto.request.diary.NewDiaryRequestDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.image.Image;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.diary.DiaryQuerydslRepository;
import com.fooddiary.api.repository.diary.DiaryRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DiaryService {

    private final DiaryRepository diaryRepository;
    private final DiaryQuerydslRepository diaryQuerydslRepository;
    private final DiaryTagRepository diaryTagRepository;
    private final TagRepository tagRepository;
    private final FileStorageService fileStorageService;
    private final ImageService imageService;
    @Value("${cloud.aws.s3.dir}")
    private String basePath;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public void createDiary(final List<MultipartFile> files, final NewDiaryRequestDTO newDiaryRequestDTO,
                            final User user) {
        final LocalDateTime dateTime = newDiaryRequestDTO.getCreateTime();
        final int year = dateTime.getYear();
        final int month = dateTime.getMonthValue();
        final int day = dateTime.getDayOfMonth();
        final int todayDiaryCount = diaryRepository.getByYearAndMonthAndDayCount(year, month, day,
                                                                                 user.getId());

        if (todayDiaryCount >= 10) {
            throw new BizException("register only 10 per day");
        }

        final Diary newDiary = Diary.createDiaryImage(dateTime, user, DiaryTime.ETC);
        diaryRepository.save(newDiary);
        imageService.storeImage(newDiary, files, user, SaveImageRequestDTO.builder().createTime(newDiaryRequestDTO.getCreateTime()).build());
    }

    public void addImages(final int diaryId, final List<MultipartFile> files, final NewDiaryRequestDTO newDiaryRequestDTO, final User user) {
        if (diaryRepository.getDiaryImagesCount(diaryId) + files.size() > 5) {
            throw new BizException("we allow max 5 images");
        }
        Diary diary = diaryRepository.findById(diaryId).orElse(null);
        if (diary == null) {
            throw new BizException("invalid diary id");
        }
        imageService.storeImage(diary, files, user, SaveImageRequestDTO.builder().createTime(newDiaryRequestDTO.getCreateTime()).build());
    }

    public DiaryDetailResponseDTO getDiaryDetail(final int id, final User user) {
        Diary diary = diaryRepository.findDiaryAndImagesById(id).orElse(null);
        if (diary == null) {
            throw new BizException("invalid diary id");
        }
        DiaryDetailResponseDTO diaryDetailResponseDTO = new DiaryDetailResponseDTO();
        diaryDetailResponseDTO.setImages(imageService.getImages(diary, user));
        diaryDetailResponseDTO.setTags(diary.getDiaryTags().stream().map(tag -> tag.getTag().getTagName()).collect(Collectors.toList()));
        diaryDetailResponseDTO.setDate(diary.getTime().getCreateTime().toLocalDate());
        diaryDetailResponseDTO.setMemo(diary.getMemo());
        diaryDetailResponseDTO.setDiaryTime(diary.getDiaryTime().name());

        return diaryDetailResponseDTO;
    }

    public void updateMemo(final Integer diaryId, final DiaryMemoRequestDTO diaryMemoRequestDTO, final User user) {
        Diary diary = diaryRepository.findById(diaryId).orElse(null);
        if (diary == null) {
            throw new BizException("invalid diary id");
        }


        diaryTagRepository.deleteAll(diary.getDiaryTags());

        List<DiaryTag> diaryTagList = new ArrayList<>();
        for (String tagName : diaryMemoRequestDTO.getTags()) {
            Tag tag = tagRepository.findTagByTagName(tagName);
            if (tag == null) {
                tag = new Tag();
                tag.setTagName(tagName);
                tagRepository.save(tag);
            }
            DiaryTag diaryTag = new DiaryTag();
            diaryTag.setTag(tag);
            diaryTag.setDiary(diary);
            diaryTagList.add(diaryTag);
        }
        diaryTagRepository.saveAll(diaryTagList);

        diary.setMemo(diaryMemoRequestDTO.getMemo());
        diary.setDiaryTags(diaryTagList);
        diary.setDiaryTime(diaryMemoRequestDTO.getDiaryTime());
        diary.setUpdateAt(LocalDateTime.now());
        diaryRepository.save(diary);
    }

    public List<ThumbNailImagesDTO> getMonthlyImages(final @RequestParam int year, final @RequestParam int month, final @AuthenticationPrincipal User user) throws IOException {
        List<ThumbNailImagesDTO> thumbNailImagesDTOList = new LinkedList<>();
        List<Diary> diaryList = diaryRepository.findByYearAndMonth(year, month, user.getId());
        for (Diary diary : diaryList) {
            if (!diary.getImages().isEmpty()) {
                Image image = diary.getImages().get(0);
                ThumbNailImagesDTO thumbNailImagesDTO = new ThumbNailImagesDTO();
                thumbNailImagesDTO.setId(image.getId());
                thumbNailImagesDTO.setTime(diary.getTime());
                thumbNailImagesDTO.setBytes(fileStorageService.getObject(ImageUtils.getDirPath(basePath, user) + image.getThumbnailFileName()));
                thumbNailImagesDTOList.add(thumbNailImagesDTO);
            }
        }
        return thumbNailImagesDTOList;
    }

}
