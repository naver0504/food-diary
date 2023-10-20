package com.fooddiary.api.controller;

import com.fooddiary.api.dto.response.timeline.TimeLineResponseDTO;
import com.fooddiary.api.dto.response.timeline.TimelineDiaryDTO;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.timeline.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/timeline")
@RequiredArgsConstructor
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping("/show")
    public ResponseEntity<List<TimeLineResponseDTO>> showTimeLine(final @RequestParam LocalDate date, final @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(timelineService.getTimeline(date, user));
    }

    /**
     * 다임라인 화면에서 flicking할때 사용됩니다.
     * @param date 선택날짜
     * @param startId 시작 diary id
     * @param user spring context에서 제공되는 사용자 정보
     * @return TimelineDiaryDTO 리스트 형식
     */
    @GetMapping("/show/more-diary")
    public ResponseEntity<List<TimelineDiaryDTO>> showMoreDiary(final @RequestParam LocalDate date, final @RequestParam int startId, final @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(timelineService.getMoreDiary(date, startId, user));
    }

}
