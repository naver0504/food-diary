package com.fooddiary.api.dto.response.diary;

import com.fasterxml.jackson.annotation.JsonFormat;
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
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
    private List<TagResponse> tags;
    private String place;
    private Double longitude = -200D;
    private Double latitude = -200D;
    private List<ImageResponseDTO> images;

    @Getter
    @Setter
    public static class TagResponse {
        private long id;
        private String name;
    }
}
