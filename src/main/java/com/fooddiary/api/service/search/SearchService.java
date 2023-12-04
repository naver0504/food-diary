package com.fooddiary.api.service.search;

import com.fooddiary.api.common.exception.BizException;
import com.fooddiary.api.dto.request.search.CategoryType;
import com.fooddiary.api.dto.response.search.DiarySearchResponseDTO;
import com.fooddiary.api.dto.response.search.DiarySearchSQLDTO;
import com.fooddiary.api.dto.response.search.SearchSQLDTO;
import com.fooddiary.api.dto.response.timeline.TimelineDiaryDTO;
import com.fooddiary.api.entity.diary.DiaryTime;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.diary.DiaryTagQuerydslRepository;
import com.fooddiary.api.repository.search.SearchRepository;
import com.fooddiary.api.service.ImageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    private final ImageService imageService;
    private final SearchRepository searchRepository;
    private final DiaryTagQuerydslRepository diaryTagQuerydslRepository;



    public List<DiarySearchResponseDTO> getSearchResultWithoutCondition(final User user, final int offset ) {
        final List<DiarySearchResponseDTO> diarySearchResponseDTOList = new ArrayList<>();

        if (!diaryTagQuerydslRepository.existByUserId(user.getId())) {
            List<SearchSQLDTO> resultList = searchRepository.getSearchResultWithoutConditionAndTag(user.getId(), PageRequest.of(offset, 4));
            resultList.forEach(
                    searchSQLDTO -> {
                        final String categoryName = searchSQLDTO.getCategoryName();
                        final CategoryType categoryType = searchSQLDTO.getCategoryType();
                        final List<TimelineDiaryDTO> diaryList = new ArrayList<>();
                        if(CategoryType.DIARY_TIME.equals(categoryType)) {
                            setDiaryList(user, diaryList, searchRepository.getSearchResultWithDiaryTime(user.getId(), DiaryTime.valueOf(categoryName), PageRequest.of(0, 3)));
                            addDiarySearchResponseDTO(diarySearchResponseDTOList, categoryName, diaryList, CategoryType.DIARY_TIME);

                        } else  {
                            setDiaryList(user, diaryList, searchRepository.getSearchResultWithPlace(user.getId(), categoryName, PageRequest.of(0, 3)));
                            addDiarySearchResponseDTO(diarySearchResponseDTOList, categoryName, diaryList, CategoryType.PLACE);

                        }
                    }
            );
        } else {
            List<SearchSQLDTO> resultList = searchRepository.getSearchResultWithoutCondition(user.getId(), PageRequest.of(offset, 4));
            resultList.forEach(
                    searchSQLDTO -> {
                        final String categoryName = searchSQLDTO.getCategoryName();
                        final CategoryType categoryType = searchSQLDTO.getCategoryType();
                        final List<TimelineDiaryDTO> diaryList = new ArrayList<>();
                        if(CategoryType.PLACE.equals(categoryType)) {
                            setDiaryList(user, diaryList, searchRepository.getSearchResultWithPlace(user.getId(), categoryName, PageRequest.of(0, 3)));
                            addDiarySearchResponseDTO(diarySearchResponseDTOList, categoryName, diaryList, CategoryType.PLACE);

                        } else {
                            setDiaryList(user, diaryList, searchRepository.getSearchResultWithTag(user.getId(), categoryName, PageRequest.of(0, 3)));
                            addDiarySearchResponseDTO(diarySearchResponseDTOList, categoryName, diaryList, CategoryType.TAG);
                        }
                    }
            );
        }
        return diarySearchResponseDTOList;
    }

    public List<TimelineDiaryDTO> getMoreSearchResult(final User user, final String categoryName, final int offset, final CategoryType categoryType) {
        final List<TimelineDiaryDTO> diaryList = new ArrayList<>();
        if(categoryType == null) {
            throw new BizException("잘못된 카테고리 타입입니다.");
        }
        switch (categoryType) {
            case DIARY_TIME:
                final DiaryTime diaryTime = DiaryTime.valueOf(categoryName);
                setDiaryList(user, diaryList, searchRepository.getSearchResultWithDiaryTime(user.getId(), diaryTime, PageRequest.of(offset, 3)));
                break;
            case PLACE:
                setDiaryList(user, diaryList, searchRepository.getSearchResultWithPlace(user.getId(), categoryName, PageRequest.of(offset, 3)));
                break;
            case TAG:
                setDiaryList(user, diaryList, searchRepository.getSearchResultWithTag(user.getId(), categoryName, PageRequest.of(offset, 3)));
                break;
        }
        return diaryList;
    }


    public List<DiarySearchResponseDTO> getSearchResultWithCondition(final User user, final String searchCond) {
        final List<DiarySearchResponseDTO> diarySearchResponseDTOList = new ArrayList<>();

        if(!StringUtils.hasText(searchCond)) return diarySearchResponseDTOList;

        final List<String> diaryTimeList = DiaryTime.getTime(searchCond).stream().map(DiaryTime::name).collect(Collectors.toList());
        final String condition = "%" + searchCond + "%";
        final List<SearchSQLDTO> searchResultWithCondition;
        try {
            searchResultWithCondition = searchRepository.getSearchResultWithCondition(user.getId(), condition, diaryTimeList);
        } catch (IllegalArgumentException e) {
            log.error("검색 결과가 없습니다. searchCond : {}", searchCond);
            return diarySearchResponseDTOList;
        }

        searchResultWithCondition
                .forEach(
                        searchSQLDTO -> {
                            final String categoryName = searchSQLDTO.getCategoryName();
                            final CategoryType categoryType = searchSQLDTO.getCategoryType();
                            final List<TimelineDiaryDTO> diaryList = new ArrayList<>();
                            switch (categoryType) {
                                case DIARY_TIME:
                                    final DiaryTime diaryTime = DiaryTime.valueOf(categoryName);
                                    searchRepository.getStatisticsSearchResultWithDiaryTimeNoLimit(user.getId(), diaryTime)
                                            .forEach(
                                                    diary -> {
                                                        diaryList.add(createTimeLineDiaryDTO(user, diary));
                                                    }
                                            );
                                    break;
                                case PLACE:
                                    searchRepository.getSearchResultWithPlaceNoLimit(user.getId(), categoryName)
                                            .forEach(
                                                    diary -> {
                                                        diaryList.add(createTimeLineDiaryDTO(user, diary));
                                                    }
                                            );
                                    break;
                                case TAG:
                                    searchRepository.getSearchResultWithTagNoLimit(user.getId(), categoryName)
                                            .forEach(
                                                    diary -> {
                                                        diaryList.add(createTimeLineDiaryDTO(user, diary));
                                                    }
                                            );
                                    break;
                            }
                            addDiarySearchResponseDTO(diarySearchResponseDTOList, categoryName, diaryList, categoryType);
                        }
                );
        return diarySearchResponseDTOList;
    }

    @NotNull
    private TimelineDiaryDTO createTimeLineDiaryDTO(final User user, final DiarySearchSQLDTO diary) {
        final TimelineDiaryDTO timelineDiaryDTO = new TimelineDiaryDTO();
        timelineDiaryDTO.setDiaryId(diary.getId());
        timelineDiaryDTO.setBytes(imageService.getImage(diary.getThumbnailFileName(), user));
        return timelineDiaryDTO;
    }

    private void setDiaryList(final User user, final List<TimelineDiaryDTO> diaryList, final List<DiarySearchSQLDTO> diarySearchSQLDTOList) {
        diarySearchSQLDTOList.forEach(
                diary -> {
                    final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
                    diaryList.add(timelineDiaryDTO);
                }
        );
    }
    private void addDiarySearchResponseDTO(final List<DiarySearchResponseDTO> diarySearchResponseDTOList, final String tagName,
                                           final List<TimelineDiaryDTO> diaryList, final CategoryType categoryType) {
        final DiarySearchResponseDTO diarySearchResponseDTO = DiarySearchResponseDTO.builder()
                .diaryList(diaryList)
                .categoryType(categoryType)
                .count(diaryList.size())
                .categoryName(tagName)
                .build();
        diarySearchResponseDTOList.add(diarySearchResponseDTO);
    }

}
