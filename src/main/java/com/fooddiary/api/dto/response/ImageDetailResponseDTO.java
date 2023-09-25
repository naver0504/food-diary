package com.fooddiary.api.dto.response;


import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Builder
@Getter
public class ImageDetailResponseDTO {

    private String memo;
    private String TimeStatus;
    private TimeDetailDTO timeDetail;
    private List<String> tags;
    private List<TimeLineResponseDTO.ImageResponseDTO> images;
}
