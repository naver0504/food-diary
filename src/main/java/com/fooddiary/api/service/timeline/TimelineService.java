package com.fooddiary.api.service.timeline;

import com.fooddiary.api.dto.response.timeline.TimeLineResponseDTO;
import com.fooddiary.api.dto.response.timeline.TimelineDiaryDTO;
import com.fooddiary.api.dto.response.timeline.TimelineDiaryDslQueryDTO;
import com.fooddiary.api.dto.response.timeline.TimelineDiaryImageSQLDTO;
import com.fooddiary.api.entity.diary.Image;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.timeline.TimelineQuerydslRepository;
import com.fooddiary.api.repository.timeline.TimelineRepository;
import com.fooddiary.api.service.ImageService;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final TimelineQuerydslRepository timelineQuerydslRepository;
    private final TimelineRepository timelineRepository;
    private final ImageService imageService;
    public List<TimeLineResponseDTO> getTimeline(final LocalDate date, final User user) {

        List<TimelineDiaryDslQueryDTO> dateList = timelineQuerydslRepository.getTimeLineDate(date, user.getId());
        List<TimelineDiaryImageSQLDTO> diaryList = new ArrayList<>();

        dateList.forEach(d -> diaryList.addAll(timelineRepository.getTimeLineDiaryWithLatestImage(user.getId(),
                                                               LocalDate.parse(d.getDate(), DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(),
                                                               LocalDate.parse(d.getDate(), DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay().plusDays(1).minusSeconds(1L).withNano(999999000),
                         PageRequest.of(0, 5))));

        List<TimeLineResponseDTO> timeLineResponseDTOList = new ArrayList<>();
        Map<LocalDate, TimeLineResponseDTO> timelineMap = new LinkedHashMap<>();
        diaryList.forEach(diary -> {
            // diary.setImages(diary.getImages().stream().limit(1).collect(Collectors.toList()));
            Image image = new Image();
            image.setStoredFileName(diary.getStoredFileName());
            image.setId(diary.getImageId());

            if (timelineMap.get(diary.getCreateTime().toLocalDate()) == null) {
                TimeLineResponseDTO timeLineResponseDTO = new TimeLineResponseDTO();
                timeLineResponseDTO.setDate(diary.getCreateTime().toLocalDate());

                List<TimelineDiaryDTO> diaries = new ArrayList<>();
                TimelineDiaryDTO innerDiary = new TimelineDiaryDTO();
                innerDiary.setBytes(imageService.getImages(List.of(image), user).get(0).getBytes());
                innerDiary.setDiaryId(diary.getId());

                diaries.add(innerDiary);
                timeLineResponseDTO.setDiaryList(diaries);
                timelineMap.put(diary.getCreateTime().toLocalDate(), timeLineResponseDTO);
            } else {
                TimeLineResponseDTO timeLineResponseDTO = timelineMap.get(diary.getCreateTime().toLocalDate());

                TimelineDiaryDTO innerDiary = new TimelineDiaryDTO();
                innerDiary.setBytes(imageService.getImages(List.of(image), user).get(0).getBytes());
                innerDiary.setDiaryId(diary.getId());

                timeLineResponseDTO.getDiaryList().add(innerDiary);
            }

        });

        for (LocalDate diaryDate : timelineMap.keySet()) {
            timeLineResponseDTOList.add(timelineMap.get(diaryDate));
        }

        return timeLineResponseDTOList;
    }

    public List<TimelineDiaryDTO> getMoreDiary(final LocalDate date, final int offset, final User user) {
        List<TimelineDiaryDTO> timelineDiaryDTOList = new ArrayList<>();
        List<TimelineDiaryImageSQLDTO> diaryList = timelineRepository.getTimeLineDiaryWithLatestImage(user.getId(),
                                                           date.atStartOfDay(),
                                                           date.atStartOfDay().plusDays(1).minusSeconds(1L).withNano(999999000),
                                                           PageRequest.of(offset, 5));

        diaryList.forEach(diary -> {
            Image image = new Image();
            image.setStoredFileName(diary.getStoredFileName());
            image.setId(diary.getImageId());

            TimelineDiaryDTO timelineDiaryDTO = new TimelineDiaryDTO();
            timelineDiaryDTO.setDiaryId(diary.getId());
            timelineDiaryDTO.setBytes(imageService.getImages(List.of(image), user).get(0).getBytes());
            timelineDiaryDTOList.add(timelineDiaryDTO);
        });

        return timelineDiaryDTOList;
    }
}
