package com.fooddiary.api.service.timeline;

import com.fooddiary.api.dto.response.timeline.TimeLineResponseDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.timeline.TimelineQuerydslRepository;
import com.fooddiary.api.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final TimelineQuerydslRepository timelineQuerydslRepository;
    private final ImageService imageService;
    public List<TimeLineResponseDTO> getTimeline(final LocalDate date, final User user) {
        List<Diary> diaryList = timelineQuerydslRepository.getTimeLineDiary(date, user.getId());
        List<TimeLineResponseDTO> timeLineResponseDTOList = new ArrayList<>();
        diaryList.forEach(diary -> {
            diary.setImages(diary.getImages().stream().limit(1).collect(Collectors.toList()));
            timeLineResponseDTOList.add(TimeLineResponseDTO.builder()
                    .date(diary.getCreateTime().toLocalDate())
                    //.diaryList(imageService.getImages(diary, user).get(0))
                    .build());

        });

        return timeLineResponseDTOList;
    }

    public List<TimeLineResponseDTO> getMoreImage(final LocalDate date, final int startId, final User user) {
        List<Diary> diaryList = timelineQuerydslRepository.getTimeLineDiary(date, user.getId());
        List<TimeLineResponseDTO> timeLineResponseDTOList = new ArrayList<>();
        diaryList.forEach(diary -> {
            diary.setImages(diary.getImages().stream().limit(5).collect(Collectors.toList()));
            timeLineResponseDTOList.add(TimeLineResponseDTO.builder()
                    .date(diary.getCreateTime().toLocalDate())
                    //.images(imageService.getImages(diary, user))
                    .build());

        });

        return timeLineResponseDTOList;
    }
}
