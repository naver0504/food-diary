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
                            searchRepository.getSearchResultWithDiaryTime(user.getId(), DiaryTime.valueOf(categoryName), PageRequest.of(0, 3))
                                    .forEach(
                                    diary -> {
                                        final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
                                        diaryList.add(timelineDiaryDTO);
                                    }
                            );
                        } else {
                            searchRepository.getSearchResultWithPlace(user.getId(), categoryName, PageRequest.of(0, 3))
                                    .forEach(
                                    diary -> {
                                        final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
                                        diaryList.add(timelineDiaryDTO);
                                    }
                            );
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
                        if (searchSQLDTO.getCategory().startsWith("#")) {
                            final String tagName = getTagName(searchSQLDTO);
                            searchRepository.getSearchResultWithTag(user.getId(), tagName, PageRequest.of(0, 3))
                                    .forEach(
                                    diary -> {
                                        final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
                                        diaryList.add(timelineDiaryDTO);
                                    }
                            );
                        } else {
                            searchRepository.getSearchResultWithPlace(user.getId(), categoryName, PageRequest.of(0, 3))
                                    .forEach(
                                    diary -> {
                                        final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
                                        diaryList.add(timelineDiaryDTO);
                                    }
                            );
                        }
                        addDiarySearchResponseDTO(diarySearchResponseDTOList, categoryName, diaryList, searchSQLDTO.getCountNum());
                    }
            );
        }
        return diarySearchResponseDTOList;
    }

    @NotNull
    private static String getTagName(SearchSQLDTO searchSQLDTO) {
        return searchSQLDTO.getCategory().substring(1);
    }

    /***
     * 검색 조건을 포함하는 태그 목록을 반환한다.
     *
     */
    public List<DiarySearchResponseDTO> getSearchResultWithCondition(final User user, final String searchCond) {
        final List<DiarySearchResponseDTO> diarySearchResponseDTOList = new ArrayList<>();
        searchQuerydslRepository.getTagNameListContainSearchCond(user, searchCond).forEach(
                tagName -> {
                    final List<DiarySearchSQLDTO.DiarySearchWithTagSQLDTO> resultList = searchRepository.getSearchResultWithTagNoLimit(user.getId(), tagName);
                    final List<TimelineDiaryDTO> diaryList = new ArrayList<>();
                    resultList.forEach(
                            diary -> {
                                final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
                                diaryList.add(timelineDiaryDTO);
                            }
                    );
                    addDiarySearchResponseDTO(diarySearchResponseDTOList, tagName, diaryList, diaryList.size());
                }
        );
        return diarySearchResponseDTOList;    }

    private void addDiarySearchResponseDTO(final List<DiarySearchResponseDTO> diarySearchResponseDTOList, final String tagName, final List<TimelineDiaryDTO> diaryList, final int size) {
        final DiarySearchResponseDTO diarySearchResponseDTO = DiarySearchResponseDTO.builder()
                .diaryList(diaryList)
                .count(diaryList.size())
                .categoryName(tagName)
                .build();
        diarySearchResponseDTOList.add(diarySearchResponseDTO);
    }

    public DiarySearchResponseDTO getStatisticSearchResultWithCondition(final User user, String searchCond) {
        final DiarySearchResponseDTO diarySearchResponseDTO = new DiarySearchResponseDTO();
        final List<TimelineDiaryDTO> diaryList = new ArrayList<>();
        /***
         * DiaryTime 으로 통계 검색 시
         */

        if(DiaryTime.getTime(searchCond) != null) {
            final DiaryTime diaryTime = DiaryTime.getTime(searchCond);
            final List<DiarySearchSQLDTO.DiarySearchWithDiaryTimeSQLDTO> resultList = searchRepository.getSearchResultWithDiaryTimeNoLimit(user.getId(), diaryTime);
            resultList.forEach(
                    diary -> {
                        final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
                        diaryList.add(timelineDiaryDTO);
                    }
            );
            searchCond = diaryTime.name();
        }
        /***
         * 장소로 통계 검색 시
         */
        else if(searchQuerydslRepository.existByPlace(user, searchCond)) {
            final List<DiarySearchSQLDTO.DiarySearchWithPlaceSQLDTO> resultList = searchRepository.getSearchResultWithPlaceNoLimit(user.getId(), searchCond);
            resultList.forEach(
                    diary -> {
                        final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
                        diaryList.add(timelineDiaryDTO);
                    }
            );
        }
        /***
         * 태그로 통계 검색 시
         */
        else {
            final List<DiarySearchSQLDTO.DiarySearchWithTagSQLDTO> resultList = searchRepository.getSearchResultWithTagNoLimit(user.getId(), searchCond);
            resultList.forEach(
                    diary -> {
                        final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
                        diaryList.add(timelineDiaryDTO);
                    }
            );
        }
        diarySearchResponseDTO.setCategoryName(searchCond);
        diarySearchResponseDTO.setDiaryList(diaryList);
        diarySearchResponseDTO.setCount(diaryList.size());

        return diarySearchResponseDTO;


    }

    @NotNull
    private TimelineDiaryDTO createTimeLineDiaryDTO(final User user, final DiarySearchSQLDTO diary) {
        final TimelineDiaryDTO timelineDiaryDTO = new TimelineDiaryDTO();
        timelineDiaryDTO.setDiaryId(diary.getId());
        timelineDiaryDTO.setBytes(imageService.getImage(diary.getThumbnailFileName(), user));
        return timelineDiaryDTO;
    }

//
//    public List<DiarySearchResponseDTO> getSearchResultWithoutCondition(final User user)  {
//
//        List<DiarySearchResponseDTO> diarySearchResponseDTOList = new ArrayList<>();
//        /***
//         * 사용자의 Diary가 없을 시
//         * @return Null List
//         */
//        if(diaryQuerydslRepository.existByUserId(user.getId()) == false) {
//            return diarySearchResponseDTOList;
//        }
//
//        final Map<String, List<TimelineDiaryDTO>> diaryListMap = new LinkedHashMap<>();
//        /***
//         * 사용자의 DiaryTag가 없을 시
//         */
//        if (diaryTagQuerydslRepository.existByUserId(user.getId()) == false) {
//            final List<DiarySearchSQLDTO.DiarySearchWithDiaryTimeSQLDTO> diaryList = diaryRepository.getSearchResultWithLatestImageAndNoTag(user.getId());
//
//            diaryList.forEach(
//                    diary -> {
//                        final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
//                        updateDiaryListMap(diaryListMap, diary.getDiaryTime().getCode(), timelineDiaryDTO);
//                        if(!Objects.isNull(diary.getPlace())) {
//                            updateDiaryListMap(diaryListMap, diary.getPlace(), timelineDiaryDTO);
//                        }
//                    }
//            );
//
//            toDTOList(diarySearchResponseDTOList, diaryListMap);
//
//        }
//        /***
//         * 태그가 있을 시
//         */
//        else {
//            final List<DiarySearchSQLDTO.DiarySearchWithTagSQLDTO> diaryList = diaryRepository.getSearchResultWithLatestImageAndTag(user.getId());
//            final List<Long> diaryIdList = new ArrayList<>();
//            diaryList.forEach(
//                    diary -> {
//                        final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
//                        updateDiaryListMap(diaryListMap, "#"+ diary.getTagName(), timelineDiaryDTO);
//                        if(!Objects.isNull(diary.getPlace())) {
//                            diaryIdList.add(Long.valueOf(diary.getId()));
//                            updateDiaryListMap(diaryListMap, diary.getPlace(), timelineDiaryDTO);
//                        }
//                    }
//            );
//            diaryRepository.getSearchResultWithLatestImageWherePlaceIsNotNull(user.getId(), diaryIdList).forEach(
//                    diary -> {
//                        final TimelineDiaryDTO timelineDiaryDTO = createTimeLineDiaryDTO(user, diary);
//                        updateDiaryListMap(diaryListMap, diary.getPlace(), timelineDiaryDTO);
//                    });
//
//            toDTOList(diarySearchResponseDTOList, diaryListMap);
//        }
//
//        diarySearchResponseDTOList.sort(new Comparator<DiarySearchResponseDTO>() {
//            @Override
//            public int compare(DiarySearchResponseDTO o1, DiarySearchResponseDTO o2) {
//                if(o1.getDiaryList().size() == o2.getDiaryList().size()) {
//                    return o1.getCategoryName().compareTo(o2.getCategoryName());
//                } else {
//                    return o2.getDiaryList().size() - o1.getDiaryList().size();
//                }
//            }
//        });
//
//        return diarySearchResponseDTOList;
//
//
//    }
//

//
//    private static void toDTOList(final List<DiarySearchResponseDTO> diarySearchResponseDTOList, final Map<String, List<TimelineDiaryDTO>> diaryListMap) {
//        diaryListMap.forEach(
//                (key, value) -> {
//                    final DiarySearchResponseDTO diarySearchResponseDTO = DiarySearchResponseDTO.builder()
//                            .categoryName(key)
//                            .diaryList(value)
//                            .build();
//                    diarySearchResponseDTOList.add(diarySearchResponseDTO);
//                }
//        );
//    }
//
//    private static void updateDiaryListMap(final Map<String, List<TimelineDiaryDTO>> diaryTimeListMap, final String categoryName, final TimelineDiaryDTO timelineDiaryDTO) {
//        if(diaryTimeListMap.containsKey(categoryName)) {
//            diaryTimeListMap.get(categoryName).add(timelineDiaryDTO);
//        } else {
//            final List<TimelineDiaryDTO> timelineDiaryDTOList = new ArrayList<>();
//            timelineDiaryDTOList.add(timelineDiaryDTO);
//            diaryTimeListMap.put(categoryName, timelineDiaryDTOList);
//        }
//    }

}
