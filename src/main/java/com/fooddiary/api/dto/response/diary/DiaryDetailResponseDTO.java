package com.fooddiary.api.dto.response.diary;

import com.fooddiary.api.dto.response.TimeDetailDTO;
import com.fooddiary.api.dto.response.image.ImageResponseDTO;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class DiaryDetailResponseDTO {
    private String memo;
    private String diaryTime;
    private LocalDate date;
    private List<String> tags;
    private List<ImageResponseDTO> images;
}
