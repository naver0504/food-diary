package com.fooddiary.api.dto.response.diary;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;
import java.util.List;

@Getter
@Setter
public class DiaryMemoResponseDTO {
    private String memo;
    private String diaryTime;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate date;
    private List<DiaryDetailResponseDTO.TagResponse> tags;
    private String place;
    private Double longitude = -200D;
    private Double latitude = -200D;

    @Getter
    @Setter
    public static class TagResponse {
        private long id;
        private String name;
    }
}
