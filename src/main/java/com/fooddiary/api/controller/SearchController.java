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

    @GetMapping
    public ResponseEntity<List<DiarySearchResponseDTO>> getSearchResult(final @AuthenticationPrincipal User user,
                                                                        final @RequestParam(value = "offset", defaultValue = "0") int offset) {
        return ResponseEntity.ok(searchService.getSearchResultWithoutCondition(user, offset));
    }

    @GetMapping("/more-diary")
    public ResponseEntity<List<TimelineDiaryDTO>> getMoreSearchResult(final @AuthenticationPrincipal User user,
                                                                      final @RequestParam String searchCond,
                                                                      final @RequestParam(value = "offset", defaultValue = "1") int offset,
                                                                      final @RequestParam("categoryType") String categoryType) {

        return ResponseEntity.ok(searchService.getMoreSearchResult(user, searchCond,offset, CategoryType.fromString(categoryType)));
    }

    @GetMapping("/statistics")
    public ResponseEntity<DiarySearchResponseDTO> getStatisticsSearchResult(final @AuthenticationPrincipal User user,
                                                                            final @RequestParam("searchCond") String searchCond,
                                                                            final @RequestParam("categoryType") String categoryType) {
        return ResponseEntity.ok(searchService.getStatisticSearchResult(user, searchCond, CategoryType.fromString(categoryType)));
    }

    @GetMapping("/condition")
    public ResponseEntity<List<DiarySearchResponseDTO>> getSearchResultWithCondition(final @AuthenticationPrincipal User user, final @RequestParam("searchCond") String searchCond) {
        return ResponseEntity.ok(searchService.getSearchResultWithCondition(user, searchCond.trim()));
    }
}
