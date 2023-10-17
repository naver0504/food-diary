package com.fooddiary.api.service.timeline;

import com.fooddiary.api.dto.response.TimeDetailDTO;
import com.fooddiary.api.dto.response.timeline.TimeLineResponseDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.diary.Time;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.timeline.TimelineQuerydslRepository;
import com.fooddiary.api.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final TimelineQuerydslRepository timelineQuerydslRepository;
    private final ImageService imageService;
    public List<TimeLineResponseDTO> getTimeline(final int year, final int month,
                            final int startDay, final User user) {
        List<Diary> diaryList = timelineQuerydslRepository.getTimeLineDayImage(year, month, startDay, user.getId());
        List<TimeLineResponseDTO> timeLineResponseDTOList = new ArrayList<>();
        diaryList.forEach(diary -> {
            diary.setImages(diary.getImages().stream().limit(5).collect(Collectors.toList()));
            timeLineResponseDTOList.add(TimeLineResponseDTO.builder()
                    .timeDetail(TimeDetailDTO.builder()
                            .month(diary.getTime().getMonth())
                            .day(diary.getTime().getDay())
                            .dayOfWeek(Time.getDayOfWeek(diary.getTime())).build())
                    .images(imageService.getImages(diary, user))
                    .build());

        });

        return timeLineResponseDTOList;
    }
}
