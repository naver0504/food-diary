package com.fooddiary.api.service.diary;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

import com.fooddiary.api.FileStorageService;
import com.fooddiary.api.common.utils.ImageUtils;
import com.fooddiary.api.dto.request.diary.DiaryMemoRequestDTO;
import com.fooddiary.api.dto.response.ThumbNailImagesDTO;
import com.fooddiary.api.dto.response.diary.DiaryDetailResponseDTO;
import com.fooddiary.api.entity.image.DiaryTime;
import com.fooddiary.api.entity.diary.DiaryTag;
import com.fooddiary.api.entity.diary.Tag;
import com.fooddiary.api.repository.ImageRepository;
import com.fooddiary.api.repository.diary.DiaryTagRepository;
import com.fooddiary.api.repository.diary.TagRepository;
import com.fooddiary.api.service.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import com.fooddiary.api.common.exception.BizException;
import com.fooddiary.api.dto.request.SaveImageRequestDTO;
import com.fooddiary.api.dto.request.diary.NewDiaryRequestDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.diary.Image;
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
    private final ImageRepository imageRepository;
    private final TagRepository tagRepository;
    private final FileStorageService fileStorageService;
    private final ImageService imageService;
    @Value("${cloud.aws.s3.dir}")
    private String basePath;

    @Value("${cloud.aws.s3.bucket}")
    private String bucket;

    public void createDiary(final List<MultipartFile> files, final LocalDateTime createTime, final List<NewDiaryRequestDTO> newDiaryRequestDTOList,
                            final User user) {
        final int year = createTime.getYear();
        final int month = createTime.getMonthValue();
        final int day = createTime.getDayOfMonth();
        final int todayDiaryCount = diaryRepository.getByYearAndMonthAndDayCount(year, month, day,
                                                                                 user.getId());

        if (todayDiaryCount >= 10) {
            throw new BizException("register only 10 per day");
        }

        final Diary newDiary = Diary.createDiaryImage(createTime, user, DiaryTime.ETC);
        diaryRepository.save(newDiary);
        List<SaveImageRequestDTO> saveImageRequestDTOList = new ArrayList<>();
        newDiaryRequestDTOList.forEach(data -> saveImageRequestDTOList.add(SaveImageRequestDTO.builder()
                .latitude(data.getLatitude())
                .longitude(data.getLongitude()).build()));
        imageService.storeImage(newDiary, files, user, createTime, saveImageRequestDTOList);
    }

    public void addImages(final int diaryId, final List<MultipartFile> files, final List<NewDiaryRequestDTO> newDiaryRequestDTOList, final User user) {
        if (diaryRepository.getDiaryImagesCount(diaryId) + files.size() > 5) {
            throw new BizException("we allow max 5 images");
        }
        Diary diary = diaryRepository.findById(diaryId).orElse(null);
        if (diary == null) {
            throw new BizException("invalid diary id");
        }

        List<SaveImageRequestDTO> saveImageRequestDTOList = new ArrayList<>();
        newDiaryRequestDTOList.forEach(data -> saveImageRequestDTOList.add(SaveImageRequestDTO.builder()
                .latitude(data.getLatitude())
                .longitude(data.getLongitude()).build()));

        imageService.storeImage(diary, files, user, diary.getTime().getCreateTime(), saveImageRequestDTOList);
        diaryRepository.save(diary);
    }

    public void updateImage(final int imageId, final MultipartFile file, final User user, final NewDiaryRequestDTO newDiaryRequestDTO) {
        Image image = imageRepository.findById(imageId).orElse(null);
        if (image == null) {
            throw new BizException("invalid image id");
        }
        Diary diary = image.getDiary();

        if (!diary.getUser().getId().equals(user.getId())) {
            throw new BizException("no permission user");
        }
        imageService.updateImage(image, file, user, newDiaryRequestDTO);
        diary.setUpdateAt(LocalDateTime.now());
        diaryRepository.save(diary);
    }

    public DiaryDetailResponseDTO getDiaryDetail(final int id, final User user) {
        Diary diary = diaryRepository.findDiaryAndImagesById(id).orElse(null);
        if (diary == null) {
            throw new BizException("invalid diary id");
        }
        DiaryDetailResponseDTO diaryDetailResponseDTO = new DiaryDetailResponseDTO();
        diaryDetailResponseDTO.setImages(imageService.getImages(diary, user));
        diaryDetailResponseDTO.setTags(diary.getDiaryTags().stream().map(tag -> {
            DiaryDetailResponseDTO.TagResponseDTO tagResponseDTO = new DiaryDetailResponseDTO.TagResponseDTO();
            tagResponseDTO.setName(tag.getTag().getTagName());
            tagResponseDTO.setId(tag.getId());
            return tagResponseDTO;
        }).collect(Collectors.toList()));
        diaryDetailResponseDTO.setDate(diary.getTime().getCreateTime().toLocalDate());
        diaryDetailResponseDTO.setMemo(diary.getMemo());
        diaryDetailResponseDTO.setDiaryTime(diary.getDiaryTime().name());

        return diaryDetailResponseDTO;
    }

    @Transactional(rollbackFor = Exception.class)
    public void updateMemo(final Integer diaryId, final DiaryMemoRequestDTO diaryMemoRequestDTO, final User user) {
        Diary diary = diaryRepository.findById(diaryId).orElse(null);
        if (diary == null) {
            throw new BizException("invalid diary id");
        }

        Map<Long, DiaryTag> diaryTagMap = new HashMap<>();
        diary.getDiaryTags().forEach(obj -> diaryTagMap.put(obj.getId(), obj));

        Set<String> tagNames = diaryMemoRequestDTO.getTags().stream().map(DiaryMemoRequestDTO.TagRequestDTO::getName).filter(StringUtils::hasText).collect(Collectors.toSet());
        if (tagNames.size() != diaryMemoRequestDTO.getTags().size()) {
            throw new BizException("이름 있고 중복되지 않은 태그만 입력해주세요");
        }

        List<DiaryTag> diaryTagList = new ArrayList<>();
        diaryMemoRequestDTO.getTags().forEach(tagRequestDTO -> {
            if (diaryTagMap.get(tagRequestDTO.getId()) == null) { // 신규
                Tag tag = tagRepository.findTagByTagName(tagRequestDTO.getName());
                if (tag == null) {
                    tag = new Tag();
                    tag.setTagName(tagRequestDTO.getName());
                    tag.setCreateAt(LocalDateTime.now());
                    tagRepository.save(tag);
                }
                DiaryTag diaryTag = new DiaryTag();
                diaryTag.setDiary(diary);
                diaryTag.setTag(tag);
                diaryTag.setCreateAt(LocalDateTime.now());
                diaryTagList.add(diaryTag);
            } else { // 수정
                DiaryTag diaryTag = diaryTagMap.get(tagRequestDTO.getId());
                if (!diaryTag.getTag().getTagName().equals(tagRequestDTO.getName())) {
                    Tag tag = new Tag();
                    tag.setTagName(tagRequestDTO.getName());
                    tag.setCreateAt(LocalDateTime.now());
                    tagRepository.save(tag);
                    diaryTag.setTag(tag);
                    diaryTag.setUpdateAt(LocalDateTime.now());
                    diaryTagRepository.save(diaryTag);
                }
                diaryTagMap.remove(tagRequestDTO.getId());
            }
        });
        diaryTagRepository.saveAll(diaryTagList);

        // 삭제
        for (DiaryTag diaryTag : diaryTagMap.values()) {
            diaryTag.getDiary().getDiaryTags().remove(diaryTag);
            diaryTag.getTag().getDiaryTags().remove(diaryTag);
        }
        diaryTagRepository.deleteAll(diaryTagMap.values());

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
                thumbNailImagesDTO.setId(diary.getId());
                thumbNailImagesDTO.setTime(diary.getTime());
                thumbNailImagesDTO.setBytes(fileStorageService.getObject(ImageUtils.getDirPath(basePath, user) + image.getThumbnailFileName()));
                thumbNailImagesDTOList.add(thumbNailImagesDTO);
            }
        }
        return thumbNailImagesDTOList;
    }

}
