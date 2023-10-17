package com.fooddiary.api.dto.response.diary;


import com.fooddiary.api.dto.response.TimeDetailDTO;
import com.fooddiary.api.dto.response.image.ImageResponseDTO;
import com.fooddiary.api.entity.image.DiaryTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class HomeDayResponseDTO {
    private Integer id; // diary id
    private String memo;
    private DiaryTime diaryTime;
    private List<String> tags;
    private ImageResponseDTO image;
}
