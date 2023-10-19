package com.fooddiary.api.dto.request.diary;

import java.time.LocalDateTime;

import com.fooddiary.api.entity.image.DiaryTime;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceInfoDTO {
    private String place;
    private Double longitude = -200D;
    private Double latitude = -200D;
}
