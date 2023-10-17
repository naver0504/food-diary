package com.fooddiary.api.controller;

import com.fooddiary.api.dto.response.timeline.TimeLineResponseDTO;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.service.timeline.TimelineService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/timeline")
@RequiredArgsConstructor
public class TimelineController {

    private final TimelineService timelineService;

    @GetMapping("/show")
    public ResponseEntity<List<TimeLineResponseDTO>> showTimeLine(final @RequestParam int year, final @RequestParam int month,
                                                                  final @RequestParam(defaultValue = "0") int startDay, final @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(timelineService.getTimeline(year, month, startDay, user));
    }

/*
    @GetMapping("/timeline")
    public ResponseEntity<List<TimeLineResponseDTO>> showTimeLine(final @RequestParam int year, final @RequestParam int month,
                                                                  final @RequestParam(defaultValue = "31") int startDay, final @AuthenticationPrincipal User user) {
        return ResponseEntity.ok(dayImageService.getTimeLine(year, month, startDay, user));
    }

    @GetMapping("/timeline/{startImageId}")
    public ResponseEntity<List<TimeLineResponseDTO.ImageResponseDTO>> showTimeLineWithStartImageId(final @RequestParam int year, final @RequestParam int month, final @RequestParam int day,
                                                                                                   final @PathVariable int startImageId, final @AuthenticationPrincipal User user) {
        // return ResponseEntity.ok(imageService.getTimeLineImagesWithStartImageId(year, month, day, startImageId, user)); todo
        return ResponseEntity.ok(null);
    }
*/
}
