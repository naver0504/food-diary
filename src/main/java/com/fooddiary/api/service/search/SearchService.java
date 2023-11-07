package com.fooddiary.api.service.search;

import com.fooddiary.api.dto.response.search.DiarySearchResponseDTO;
import com.fooddiary.api.dto.response.search.DiarySearchSQLDTO;
import com.fooddiary.api.dto.response.search.SearchSQLDTO;
import com.fooddiary.api.dto.response.timeline.TimelineDiaryDTO;
import com.fooddiary.api.entity.diary.DiaryTime;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.diary.DiaryTagQuerydslRepository;
import com.fooddiary.api.repository.search.SearchQuerydslRepository;
import com.fooddiary.api.repository.search.SearchRepository;
import com.fooddiary.api.service.ImageService;
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

    private final ImageService imageService;
    private final SearchRepository searchRepository;
    private final SearchQuerydslRepository searchQuerydslRepository;
    private final DiaryTagQuerydslRepository diaryTagQuerydslRepository;



    public List<DiarySearchResponseDTO> getSearchResultWithoutCondition(final User user, final int offset ) {
        final List<DiarySearchResponseDTO> diarySearchResponseDTOList = new ArrayList<>();

        if (!diaryTagQuerydslRepository.existByUserId(user.getId())) {
            List<SearchSQLDTO> resultList = searchRepository.getSearchResultWithoutConditionAndTag(user.getId(), PageRequest.of(offset, 4));
            resultList.forEach(
                    searchSQLDTO -> {
                        final String categoryName = searchSQLDTO.getCategory();
                        final List<TimelineDiaryDTO> diaryList = new ArrayList<>();
                        if (DiaryTime.isDiaryTime(categoryName)) {
                            setDiaryList(user, diaryList, searchRepository.getSearchResultWithDiaryTime(user.getId(), DiaryTime.valueOf(categoryName), PageRequest.of(0, 3)));

                        } else {
                            setDiaryList(user, diaryList, searchRepository.getSearchResultWithPlace(user.getId(), categoryName, PageRequest.of(0, 3)));
                        }
                        addDiarySearchResponseDTO(diarySearchResponseDTOList, categoryName, diaryList, searchSQLDTO.getCountNum());
                    }
            );
        } else {
            List<SearchSQLDTO> resultList = searchRepository.getSearchResultWithoutCondition(user.getId(), PageRequest.of(offset, 4));
            resultList.forEach(
                    searchSQLDTO -> {
                        final String categoryName = searchSQLDTO.getCategory();
                        final List<TimelineDiaryDTO> diaryList = new ArrayList<>();
                        /***
                         * 태그와 장소를 구분하기 위해서 태그를 가져올 때 앞에 #을 붙여서 가져온다.
                         */
                        if (searchSQLDTO.getCategory().startsWith("#")) {
                            final String tagName = getTagName(searchSQLDTO);
                            setDiaryList(user, diaryList, searchRepository.getSearchResultWithTag(user.getId(), tagName, PageRequest.of(0, 3)));
                        } else {
                            setDiaryList(user, diaryList, searchRepository.getSearchResultWithPlace(user.getId(), categoryName, PageRequest.of(0, 3)));
                        }
                        addDiarySearchResponseDTO(diarySearchResponseDTOList, categoryName, diaryList, searchSQLDTO.getCountNum());
                    }
            );
        }
        return diarySearchResponseDTOList;
    }

    public List<TimelineDiaryDTO> getMoreSearchResult(final User user, final String searchCond, final int offset) {
        final List<TimelineDiaryDTO> diaryList = new ArrayList<>();
        if (!diaryTagQuerydslRepository.existByUserId(user.getId())) {
            final DiaryTime diaryTime = DiaryTime.getTime(searchCond);
            if (diaryTime != null) {
                setDiaryList(user, diaryList, searchRepository.getSearchResultWithDiaryTime(user.getId(), diaryTime, PageRequest.of(offset, 3)));
            } else {
                setDiaryList(user, diaryList, searchRepository.getSearchResultWithPlace(user.getId(), searchCond, PageRequest.of(offset, 3)));
            }
        } else {
            /***
             *
             * 해당 카테고리가 태그인지 아닌 지 확인 후 태그면 태그로 검색, 아니면 장소로 검색
             */
            if (diaryTagQuerydslRepository.existByUserIdAndTagName(user.getId(), searchCond)) {
                setDiaryList(user, diaryList, searchRepository.getSearchResultWithTag(user.getId(), searchCond, PageRequest.of(offset, 3)));
            } else {
                setDiaryList(user, diaryList, searchRepository.getSearchResultWithPlace(user.getId(), searchCond, PageRequest.of(offset, 3)));
            }
        }
        return diaryList;
    }





    /***
     * 검색 조건을 포함하는 태그 목록을 반환한다.
     *
     */
    public List<DiarySearchResponseDTO> getSearchResultWithCondition(final User user, final String searchCond) {
        final List<DiarySearchResponseDTO> diarySearchResponseDTOList = new ArrayList<>();
        final LinkedMultiValueMap<String, TimelineDiaryDTO> diarySearchResponseDTOMap = new LinkedMultiValueMap<>();
        if (DiaryTime.getTime(searchCond) != null) {
            final DiaryTime diaryTime = DiaryTime.getTime(searchCond);
            searchRepository.getStatisticsSearchResultWithDiaryTimeNoLimit(user.getId(), diaryTime)
                    .forEach(
                            diary -> {
                                final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
                                addDiarySearchResponseMap(diarySearchResponseDTOMap, diaryTime.name(), timelineDiaryDTO);
                            }
                    );
        }
        /**
         * 해당 검색어 조건을 포함하는 장소를 추가
         *
         */
        searchRepository.getSearchResultContainPlace(user.getId(), "%" +searchCond + "%").forEach(
                diary -> {
                    final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
                    addDiarySearchResponseMap(diarySearchResponseDTOMap, diary.getPlace(), timelineDiaryDTO);

                }
        );
        /***
         * 해당 검색어 조건을 포함하는 태그를 추가
         */
        searchRepository.getSearchResultContainTagName(user.getId(), "%" +searchCond + "%").forEach(
                diary -> {
                    final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
                    addDiarySearchResponseMap(diarySearchResponseDTOMap, diary.getTagName(), timelineDiaryDTO);
                }
        );

        diarySearchResponseDTOMap.forEach(
                (key, value) -> {
                    final DiarySearchResponseDTO diarySearchResponseDTO = DiarySearchResponseDTO.builder()
                            .diaryList(value)
                            .count(value.size())
                            .categoryName(key)
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


    public DiarySearchResponseDTO getStatisticSearchResultWithCondition(final User user, String searchCond) {
        final DiarySearchResponseDTO diarySearchResponseDTO = new DiarySearchResponseDTO();
        final List<TimelineDiaryDTO> diaryList = new ArrayList<>();
        /***
         * DiaryTime 으로 통계 검색 시
         */
        if(DiaryTime.getTime(searchCond) != null) {
            final DiaryTime diaryTime = DiaryTime.getTime(searchCond);
            setDiaryList(user, diaryList, searchRepository.getStatisticsSearchResultWithDiaryTimeNoLimit(user.getId(), diaryTime));
            searchCond = diaryTime.name();
        }
        /***
         * 장소로 통계 검색 시
         */
        else if(searchQuerydslRepository.existByPlace(user, searchCond)) {
            setDiaryList(user, diaryList, searchRepository.getStatisticsSearchResultWithPlaceNoLimit(user.getId(), searchCond));
        }
        /***
         * 태그로 통계 검색 시
         */
        else {
            setDiaryList(user, diaryList, searchRepository.getStatisticsSearchResultWithTagNoLimit(user.getId(), searchCond));
        }
        setDiarySearchResponseDTO(searchCond, diarySearchResponseDTO, diaryList);

        return diarySearchResponseDTO;
    }

    private static void setDiarySearchResponseDTO(final String searchCond, final DiarySearchResponseDTO diarySearchResponseDTO, final List<TimelineDiaryDTO> diaryList) {
        diarySearchResponseDTO.setCategoryName(searchCond);
        diarySearchResponseDTO.setDiaryList(diaryList);
        diarySearchResponseDTO.setCount(diaryList.size());
    }

    @NotNull
    private TimelineDiaryDTO createTimeLineDiaryDTO(final User user, final DiarySearchSQLDTO diary) {
        final TimelineDiaryDTO timelineDiaryDTO = new TimelineDiaryDTO();
        timelineDiaryDTO.setDiaryId(diary.getId());
        timelineDiaryDTO.setBytes(imageService.getImage(diary.getThumbnailFileName(), user));
        return timelineDiaryDTO;
    }

    @NotNull
    private String getTagName(final SearchSQLDTO searchSQLDTO) {
        return searchSQLDTO.getCategory().substring(1);
    }

    private void setDiaryList(final User user, final List<TimelineDiaryDTO> diaryList, final List<DiarySearchSQLDTO> diarySearchSQLDTOList) {
        diarySearchSQLDTOList.forEach(
                diary -> {
                    final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
                    diaryList.add(timelineDiaryDTO);
                }
        );
    }
    private void addDiarySearchResponseDTO(final List<DiarySearchResponseDTO> diarySearchResponseDTOList, final String tagName, final List<TimelineDiaryDTO> diaryList, final int size) {
        final DiarySearchResponseDTO diarySearchResponseDTO = DiarySearchResponseDTO.builder()
                .diaryList(diaryList)
                .count(diaryList.size())
                .categoryName(tagName)
                .build();
        diarySearchResponseDTOList.add(diarySearchResponseDTO);
    }

    private void addDiarySearchResponseMap(final LinkedMultiValueMap<String, TimelineDiaryDTO> map, final String categoryName, final TimelineDiaryDTO timelineDiaryDTO) {
        if (!map.containsKey(categoryName)) {
            map.add(categoryName, timelineDiaryDTO);
        } else {
            map.get(categoryName).add(timelineDiaryDTO);
        }
    }
}
