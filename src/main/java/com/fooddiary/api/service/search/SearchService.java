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
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.jetbrains.annotations.NotNull;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;

import java.util.*;

@Service
@RequiredArgsConstructor
@Slf4j
public class SearchService {

    @EqualsAndHashCode
    @Getter
    private class Pair {
        private String categoryName;
        private CategoryType categoryType;

        public Pair(final String categoryName, final CategoryType categoryType) {
            this.categoryName = categoryName;
            this.categoryType = categoryType;
        }
    }

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

    /***
     * 검색 조건을 포함하는 태그 목록을 반환한다.
     *
     */
    public List<DiarySearchResponseDTO> getSearchResultWithCondition(final User user, final String searchCond) {
        final List<DiarySearchResponseDTO> diarySearchResponseDTOList = new ArrayList<>();
        final LinkedMultiValueMap<Pair, TimelineDiaryDTO> diarySearchResponseDTOMap = new LinkedMultiValueMap<>();
        final List<DiaryTime> diaryTimeList = DiaryTime.getTime(searchCond);
        if (!diaryTimeList.isEmpty()) {
            diaryTimeList.forEach(
                    diaryTime -> {
                        final Pair pair = new Pair(diaryTime.name(), CategoryType.DIARY_TIME);
                        searchRepository.getStatisticsSearchResultWithDiaryTimeNoLimit(user.getId(), diaryTime)
                                .forEach(
                                        diary -> {
                                            final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
                                            addDiarySearchResponseMap(diarySearchResponseDTOMap, pair, timelineDiaryDTO);
                                        }
                                );
                    }
            );
        }
        /**
         * 해당 검색어 조건을 포함하는 장소를 추가
         *
         */
        searchRepository.getSearchResultContainPlace(user.getId(), "%" +searchCond + "%").forEach(
                diary -> {
                    final Pair pair = new Pair(diary.getPlace(), CategoryType.PLACE);
                    final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
                    addDiarySearchResponseMap(diarySearchResponseDTOMap, pair, timelineDiaryDTO);
                }
        );
        /***
         * 해당 검색어 조건을 포함하는 태그를 추가
         */
        searchRepository.getSearchResultContainTagName(user.getId(), "%" +searchCond + "%").forEach(
                diary -> {
                    final Pair pair = new Pair(diary.getTagName(), CategoryType.TAG);
                    final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
                    addDiarySearchResponseMap(diarySearchResponseDTOMap, pair, timelineDiaryDTO);
                }
        );

        diarySearchResponseDTOMap.forEach(
                (key, value) -> {
                    final DiarySearchResponseDTO diarySearchResponseDTO = DiarySearchResponseDTO.builder()
                            .diaryList(value)
                            .count(value.size())
                            .categoryName(key.getCategoryName())
                            .categoryType(key.getCategoryType())
                            .build();
                    diarySearchResponseDTOList.add(diarySearchResponseDTO);
                }
        );

        diarySearchResponseDTOList.sort(new Comparator<DiarySearchResponseDTO>() {
                                            @Override
                                            public int compare(DiarySearchResponseDTO o1, DiarySearchResponseDTO o2) {
                                                if (o1.getCount() == o2.getCount()) {
                                                    return o1.getCategoryName().compareTo(o2.getCategoryName());
                                                } else {
                                                    return o2.getCount() - o1.getCount();
                                                }
                                            }
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

    private void addDiarySearchResponseMap(final LinkedMultiValueMap<Pair, TimelineDiaryDTO> map, final Pair pair, final TimelineDiaryDTO timelineDiaryDTO) {
        if (!map.containsKey(pair)) {
            map.add(pair, timelineDiaryDTO);
        } else {
            map.get(pair).add(timelineDiaryDTO);
        }
    }
}
