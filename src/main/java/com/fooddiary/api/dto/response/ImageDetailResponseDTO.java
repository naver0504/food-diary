package com.fooddiary.api.dto.response;


import com.fooddiary.api.dto.response.image.ImageResponseDTO;
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
    private String timeStatus;
    private TimeDetailDTO timeDetail;
    private List<String> tags;
    private List<ImageResponseDTO> images;
}
