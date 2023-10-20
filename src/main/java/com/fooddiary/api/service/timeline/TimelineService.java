package com.fooddiary.api.service.timeline;

import com.fooddiary.api.dto.response.image.ImageResponseDTO;
import com.fooddiary.api.dto.response.timeline.TimeLineResponseDTO;
import com.fooddiary.api.dto.response.timeline.TimelineDiaryDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.timeline.TimelineQuerydslRepository;
import com.fooddiary.api.service.ImageService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TimelineService {

    private final TimelineQuerydslRepository timelineQuerydslRepository;
    private final ImageService imageService;
    public List<TimeLineResponseDTO> getTimeline(final LocalDate date, final User user) {
        List<Diary> diaryList = timelineQuerydslRepository.getTimeLineDiary(date, user.getId());
        List<TimeLineResponseDTO> timeLineResponseDTOList = new ArrayList<>();
        Map<LocalDate, TimeLineResponseDTO> timelineMap = new LinkedHashMap<>();
        diaryList.forEach(diary -> {
            diary.setImages(diary.getImages().stream().limit(1).collect(Collectors.toList()));

            if (timelineMap.get(diary.getCreateTime().toLocalDate()) == null) {
                TimeLineResponseDTO timeLineResponseDTO = new TimeLineResponseDTO();
                timeLineResponseDTO.setDate(diary.getCreateTime().toLocalDate());

                List<TimelineDiaryDTO> diaries = new ArrayList<>();
                TimelineDiaryDTO innerDiary = new TimelineDiaryDTO();
                innerDiary.setBytes(imageService.getImages(diary, user).get(0).getBytes());
                innerDiary.setDiaryId(diary.getId());

                diaries.add(innerDiary);
                timeLineResponseDTO.setDiaryList(diaries);
                timelineMap.put(diary.getCreateTime().toLocalDate(), timeLineResponseDTO);
            } else {
                TimeLineResponseDTO timeLineResponseDTO = timelineMap.get(diary.getCreateTime().toLocalDate());

                TimelineDiaryDTO innerDiary = new TimelineDiaryDTO();
                innerDiary.setBytes(imageService.getImages(diary, user).get(0).getBytes());
                innerDiary.setDiaryId(diary.getId());

                timeLineResponseDTO.getDiaryList().add(innerDiary);
            }

        });

        for (LocalDate diaryDate : timelineMap.keySet()) {
            timeLineResponseDTOList.add(timelineMap.get(diaryDate));
        }

        return timeLineResponseDTOList;
    }

    public List<TimelineDiaryDTO> getMoreDiary(final LocalDate date, final int startId, final User user) {
        List<TimelineDiaryDTO> timelineDiaryDTOList = new ArrayList<>();
        List<Diary> diaryList = timelineQuerydslRepository.getMoreDiary(date, startId, user.getId());

        diaryList.forEach(diary -> {
            TimelineDiaryDTO timelineDiaryDTO = new TimelineDiaryDTO();
            timelineDiaryDTO.setDiaryId(diary.getId());
            diary.setImages(diary.getImages().stream().limit(1).collect(Collectors.toList()));
            timelineDiaryDTO.setBytes(imageService.getImages(diary, user).get(0).getBytes());
            timelineDiaryDTOList.add(timelineDiaryDTO);
        });

        return timelineDiaryDTOList;
    }
}
