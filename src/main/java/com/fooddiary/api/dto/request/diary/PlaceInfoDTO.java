package com.fooddiary.api.dto.request.diary;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class PlaceInfoDTO {
    private String place;
    private Double longitude = -200D;
    private Double latitude = -200D;
}
