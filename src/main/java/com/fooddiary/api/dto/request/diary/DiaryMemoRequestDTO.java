package com.fooddiary.api.dto.request.diary;


import com.fooddiary.api.entity.image.DiaryTime;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DiaryMemoRequestDTO {
    private String memo;
    private DiaryTime diaryTime;
    private List<String> tags;
}
