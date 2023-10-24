package com.fooddiary.api.dto.response.image;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class ImageResponseDTO {
    private long imageId;
    private byte[] bytes;
}
