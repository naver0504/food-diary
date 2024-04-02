package com.fooddiary.api.service.diary;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.fooddiary.api.entity.diary.DiaryTime;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import com.fooddiary.api.common.exception.BizException;
import com.fooddiary.api.dto.request.diary.DiaryMemoRequestDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.diary.DiaryTag;
import com.fooddiary.api.entity.diary.Tag;
import com.fooddiary.api.repository.diary.DiaryRepository;
import com.fooddiary.api.repository.diary.DiaryTagRepository;
import com.fooddiary.api.repository.diary.TagRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class DiaryTransactionService {
    private final DiaryRepository diaryRepository;
    private final TagRepository tagRepository;
    private final DiaryTagRepository diaryTagRepository;

    @Transactional(rollbackFor = Exception.class)
    public void updateMemo(final long diaryId, final DiaryMemoRequestDTO diaryMemoRequestDTO, final Map<String, Tag> deleteTagMap) {
            final Diary diary = diaryRepository.findById(diaryId).orElse(null);
            if (diary == null) {
                throw new BizException("invalid diary id");
            }

            final Map<Long, DiaryTag> diaryTagMap = new HashMap<>();
            diary.getDiaryTags().forEach(obj -> {
                diaryTagMap.put(obj.getId(), obj);
                deleteTagMap.put(obj.getTag().getTagName(), obj.getTag());
            });

            final Set<String> tagNames = diaryMemoRequestDTO.getTags().stream()
                                                            .map(tag -> {
                                                                if (tag.getName().length() > 50) {
                                                                    throw new BizException("태그는 50자 이하로 입력부탁드립니다.");
                                                                }
                                                                return tag.getName();
                                                            })
                                                            .filter(StringUtils::hasText)
                                                            .collect(Collectors.toSet());

            if (tagNames.size() != diaryMemoRequestDTO.getTags().size()) {
                throw new BizException("이름 있고 중복되지 않은 태그만 입력해주세요");
            }
            if (tagNames.size() > 20) {
                throw new RuntimeException("태그는 20개까지만 가능합니다.");
            }
            if (StringUtils.hasLength(diaryMemoRequestDTO.getMemo()) && diaryMemoRequestDTO.getMemo().length() > 500) {
                throw new BizException("메모는 500자까지 입니다.");
            }

            final List<DiaryTag> diaryTagList = new ArrayList<>();

            diaryMemoRequestDTO.getTags().forEach(tagRequestDTO -> {
                if (diaryTagMap.get(tagRequestDTO.getId()) == null) { // 신규
                    Tag tag = tagRepository.findTagByTagName(tagRequestDTO.getName());
                    if (tag == null) {
                        tag = new Tag();
                        tag.setTagName(tagRequestDTO.getName());
                        tag.setCreateAt(LocalDateTime.now());
                        tagRepository.save(tag);
                    }
                    final DiaryTag diaryTag = new DiaryTag();
                    diaryTag.setDiary(diary);
                    diaryTag.setTag(tag);
                    diaryTag.setCreateAt(LocalDateTime.now());
                    diaryTagList.add(diaryTag);
                } else { // 수정
                    final DiaryTag diaryTag = diaryTagMap.get(tagRequestDTO.getId());
                    if (!diaryTag.getTag().getTagName().equals(tagRequestDTO.getName())) {
                        Tag tag = tagRepository.findTagByTagName(tagRequestDTO.getName());
                        if (tag == null) {
                            tag = new Tag();
                            tag.setTagName(tagRequestDTO.getName());
                            tag.setCreateAt(LocalDateTime.now());
                            tagRepository.save(tag);
                        }
                        diaryTag.setTag(tag);
                        diaryTag.setUpdateAt(LocalDateTime.now());
                        diaryTagList.add(diaryTag);
                    } else {
                        deleteTagMap.remove(diaryTag.getTag().getTagName());
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
            // not blank만 저장하도록 한다.
            if (StringUtils.hasText(diaryMemoRequestDTO.getMemo())) {
                diary.setMemo(diaryMemoRequestDTO.getMemo());
            }
            diary.setDiaryTags(diaryTagList);
            diary.setCreateTime(DiaryTime.makeCreateTime(diary.getCreateTime().toLocalDate(), diaryMemoRequestDTO.getDiaryTime()));
            diary.setDiaryTime(diaryMemoRequestDTO.getDiaryTime());
            diary.setUpdateAt(LocalDateTime.now());
            // not blank만 저장하도록 한다.
            if (StringUtils.hasText(diaryMemoRequestDTO.getPlace())) {
                diary.setPlace(diaryMemoRequestDTO.getPlace());
            }
            diary.setGeography(diaryMemoRequestDTO.getLongitude(), diaryMemoRequestDTO.getLatitude());
            diaryRepository.save(diary);
        }
}
