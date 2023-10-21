package com.fooddiary.api.dto.response.timeline;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fooddiary.api.dto.response.TimeDetailDTO;
import com.fooddiary.api.dto.response.image.ImageResponseDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.image.DayImage;
import com.fooddiary.api.entity.diary.Time;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
public class TimeLineResponseDTO {
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM")
    private LocalDate date;
    private List<TimelineDiaryDTO> diaryList;
}
