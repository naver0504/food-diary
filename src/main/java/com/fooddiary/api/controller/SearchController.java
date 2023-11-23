package com.fooddiary.api.controller;

import com.fooddiary.api.dto.request.search.CategoryType;
import com.fooddiary.api.dto.response.search.DiarySearchResponseDTO;
import com.fooddiary.api.dto.response.timeline.TimelineDiaryDTO;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.search.SearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/search")
@RequiredArgsConstructor
public class SearchController {

    private final SearchService searchService;

    /***
     *
     * 검색 메뉴 선택 시 보이는 첫 번째 페이지
     * @param user SecurityContextHolder에서 가져온 사용자 정보
     * @param offset 페이징 처리를 위한 offset
     * @return 각 카테고리 이름, 카테고리 타입, 카테고리에 해당하는 일기 리스트
     * 각 카테고리 마다 3개의 일기 썸네일 이미지를 보여준다.
     * 카테고리는 총 4개를 보여준다.
     */
    @GetMapping
    public ResponseEntity<List<DiarySearchResponseDTO>> getSearchResult(final @AuthenticationPrincipal User user,
                                                                        final @RequestParam(value = "offset", defaultValue = "0") int offset) {
        return ResponseEntity.ok(searchService.getSearchResultWithoutCondition(user, offset));
    }

    /***
     *
     * 해당 카테고리에서 추가 일기를 전달
     * @param user SecurityContextHolder에서 가져온 사용자 정보
     * @param offset 페이징 처리를 위한 offset
     * @param  categoryType 카테고리 타입
     * @param  categoryName 카테고리 이름
     * @return
     */
    @GetMapping("/more-diary")
    public ResponseEntity<List<TimelineDiaryDTO>> getMoreSearchResult(final @AuthenticationPrincipal User user,
                                                                      final @RequestParam String categoryName,
                                                                      final @RequestParam(value = "offset", defaultValue = "1") int offset,
                                                                      final @RequestParam("categoryType") String categoryType) {

        return ResponseEntity.ok(searchService.getMoreSearchResult(user, categoryName,offset, CategoryType.fromString(categoryType)));
    }

    /***
     *
     * 카테고리 클릭 시 보이는 페이지
     * @param user SecurityContextHolder에서 가져온 사용자 정보
     * @param categoryName 검색어
     * @param categoryType 카테고리 타입
     * @return 카테고리에 해당하는 일기 리스트
     */
    @GetMapping("/statistics")
    public ResponseEntity<DiarySearchResponseDTO> getStatisticsSearchResult(final @AuthenticationPrincipal User user,
                                                                            final @RequestParam("categoryName") String categoryName,
                                                                            final @RequestParam("categoryType") String categoryType) {
        return ResponseEntity.ok(searchService.getStatisticSearchResult(user, categoryName, CategoryType.fromString(categoryType)));
    }

    /***
     *
     * 검색창에 검색어 입력 시 보이는 페이지
     * @param user SecurityContextHolder에서 가져온 사용자 정보
     * @param searchCond 검색어
     * @return 검색어에 해당하는 모든 일기 리스트
     */
    @GetMapping("/condition")
    public ResponseEntity<List<DiarySearchResponseDTO>> getSearchResultWithCondition(final @AuthenticationPrincipal User user, final @RequestParam("searchCond") String searchCond) {
        return ResponseEntity.ok(searchService.getSearchResultWithCondition(user, searchCond.trim()));
    }
}
