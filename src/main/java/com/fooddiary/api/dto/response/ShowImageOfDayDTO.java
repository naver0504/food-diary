package com.fooddiary.api.dto.response;


import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fooddiary.api.entity.diary.DiaryTime;
import lombok.*;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ShowImageOfDayDTO {

    private TimeDetailDTO todayTime;
    private TimeDetailDTO beforeTime;
    private TimeDetailDTO afterTime;
    private List<ImageDTO> images;


    @Getter
    @Setter
    @AllArgsConstructor
    @NoArgsConstructor
    @Builder
    public static class ImageDTO{

        private int id;
        private byte[] bytes;
        private List<String> tags;

        @JsonIgnore
        private DiaryTime timeStatus;

        private String time;

    }
}
