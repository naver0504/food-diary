package com.fooddiary.api.dto.request.diary;


import com.fooddiary.api.entity.diary.DiaryTime;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DiaryMemoRequestDTO {
    private String memo;
    private DiaryTime diaryTime = DiaryTime.ETC;
    private List<TagRequestDTO> tags;

    private String place;
    private Double longitude = -200D;
    private Double latitude = -200D;


    @Getter
    @Setter
    public static class TagRequestDTO {
        private Long id;
        private String name;
    }
}
