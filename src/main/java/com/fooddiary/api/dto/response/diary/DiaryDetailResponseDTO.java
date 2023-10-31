package com.fooddiary.api.dto.response.diary;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DiaryDetailResponseDTO extends DiaryMemoResponseDTO {
    private List<ImageResponseDTO> images;
}
