package com.fooddiary.api.dto.request.diary;

import java.time.LocalDateTime;

import com.fooddiary.api.entity.image.DiaryTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NewDiaryRequestDTO {
    private LocalDateTime createTime;
    private DiaryTime diaryTime;
    private Double longitude = -200D;
    private Double latitude = -200D;
}
