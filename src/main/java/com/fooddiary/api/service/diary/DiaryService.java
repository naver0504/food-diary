package com.fooddiary.api.service.diary;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.util.*;
import java.util.stream.Collectors;

import com.fooddiary.api.FileStorageService;
import com.fooddiary.api.common.util.ImageUtils;
import com.fooddiary.api.dto.request.diary.DiaryMemoRequestDTO;
import com.fooddiary.api.dto.response.diary.HomeResponseDTO;
import com.fooddiary.api.dto.response.diary.DiaryDetailResponseDTO;
import com.fooddiary.api.dto.response.diary.HomeDayResponseDTO;
import com.fooddiary.api.entity.diary.*;
import com.fooddiary.api.entity.image.DiaryTime;
import com.fooddiary.api.repository.ImageRepository;
import com.fooddiary.api.repository.diary.DiaryTagRepository;
import com.fooddiary.api.repository.diary.TagRepository;
import com.fooddiary.api.service.ImageService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.fooddiary.api.common.exception.BizException;
import com.fooddiary.api.dto.request.diary.PlaceInfoDTO;
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

    public void createDiary(final List<MultipartFile> files, final LocalDate createDate, final PlaceInfoDTO placeInfoDTO,
                            final User user) {
        LocalDateTime startDate = createDate.atStartOfDay();
        LocalDateTime endDate = createDate.plusDays(1L).atStartOfDay().minusNanos(1L);
        final int todayDiaryCount = diaryRepository.getByYearAndMonthAndDayCount(startDate, endDate,
                                                                                 user.getId());

        if (todayDiaryCount >= 10) {
            throw new BizException("register only 10 per day");
        }

        final Diary newDiary = Diary.createDiary(createDate, user, DiaryTime.ETC, placeInfoDTO);
        diaryRepository.save(newDiary);
        imageService.storeImage(newDiary, files, user);
    }

    public void addImages(final int diaryId, final List<MultipartFile> files, final User user) {
        if (diaryRepository.getDiaryImagesCount(diaryId) + files.size() > 5) {
            throw new BizException("we allow max 5 images");
        }
        Diary diary = diaryRepository.findById(diaryId).orElse(null);
        if (diary == null) {
            throw new BizException("invalid diary id");
        }

        imageService.storeImage(diary, files, user);
        diaryRepository.save(diary);
    }

    public void updateImage(final int imageId, final MultipartFile file, final User user) {
        Image image = imageRepository.findById(imageId).orElse(null);
        if (image == null) {
            throw new BizException("invalid image id");
        }
        Diary diary = image.getDiary();

        if (!diary.getUser().getId().equals(user.getId())) {
            throw new BizException("no permission user");
        }
        imageService.updateImage(image.getId(), file, user);
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
            DiaryDetailResponseDTO.TagResponse tagResponse = new DiaryDetailResponseDTO.TagResponse();
            tagResponse.setName(tag.getTag().getTagName());
            tagResponse.setId(tag.getId());
            return tagResponse;
        }).collect(Collectors.toList()));
        diaryDetailResponseDTO.setDate(diary.getCreateTime().toLocalDate());
        diaryDetailResponseDTO.setMemo(diary.getMemo());
        diaryDetailResponseDTO.setPlace(diary.getPlace());
        if (diary.getGeography() != null) {
            diaryDetailResponseDTO.setLongitude(diary.getGeography().getX());
            diaryDetailResponseDTO.setLatitude(diary.getGeography().getY());
        }
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
        diary.setCreateTime(Diary.makeCreateTime(diary.getCreateTime().toLocalDate(), diaryMemoRequestDTO.getDiaryTime()));
        diary.setDiaryTime(diaryMemoRequestDTO.getDiaryTime());
        diary.setUpdateAt(LocalDateTime.now());
        diary.setPlace(diaryMemoRequestDTO.getPlace());
        diary.setGeography(diaryMemoRequestDTO.getLongitude(), diaryMemoRequestDTO.getLatitude());
        diaryRepository.save(diary);
    }

    public List<HomeResponseDTO> getHome(final YearMonth yearMonth, final User user) throws IOException {
        List<HomeResponseDTO> homeResponseDTOList = new LinkedList<>();
        LocalDateTime startDate = yearMonth.atDay(1).atStartOfDay();
        LocalDateTime endDate = yearMonth.atEndOfMonth().plusDays(1L).atStartOfDay().minusNanos(1L);
        List<Diary> diaryList = diaryRepository.findByYearAndMonth(startDate, endDate, user.getId());
        for (Diary diary : diaryList) {
            if (!diary.getImages().isEmpty()) {
                Image image = diary.getImages().get(0);
                HomeResponseDTO homeResponseDTO = new HomeResponseDTO();
                homeResponseDTO.setId(diary.getId());
                homeResponseDTO.setTime(diary.getCreateTime().toLocalDate());
                homeResponseDTO.setBytes(fileStorageService.getObject(ImageUtils.getDirPath(basePath, user) + image.getThumbnailFileName()));
                homeResponseDTOList.add(homeResponseDTO);
            }
        }
        return homeResponseDTOList;
    }

    public HomeDayResponseDTO getHomeDay(final LocalDate date, final User user) {
        List<HomeDayResponseDTO.HomeDay> homeDayList = new LinkedList<>();
        LocalDateTime startDate = date.atStartOfDay();
        LocalDateTime endDate = date.plusDays(1).atStartOfDay().minusNanos(1L);
        List<Diary> diaryList = diaryRepository.findByYearAndMonthAndDay(startDate, endDate, user.getId());

        for (Diary diary : diaryList) {
            if (!diary.getImages().isEmpty()) {
                Image image = diary.getImages().get(0);
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

        HomeDayResponseDTO homeDayResponseDTO = new HomeDayResponseDTO();
        homeDayResponseDTO.setHomeDayList(homeDayList);

        Map<String, Time> timeMap =  diaryQuerydslRepository.getBeforeAndAfterTime(date.getYear(), date.getMonthValue(), date.getDayOfMonth(), user.getId());
        if (timeMap.get("before") != null) {
            Time before = timeMap.get("before");
            homeDayResponseDTO.setBeforeDay(LocalDate.of(before.getYear(), before.getMonth(), before.getDay()));
        }
        if (timeMap.get("after") != null) {
            Time after = timeMap.get("after");
            homeDayResponseDTO.setAfterDay(LocalDate.of(after.getYear(), after.getMonth(), after.getDay()));
        }

        return homeDayResponseDTO;
    }

}
