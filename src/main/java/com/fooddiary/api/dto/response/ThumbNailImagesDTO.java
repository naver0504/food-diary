package com.fooddiary.api.dto.response;

import com.fooddiary.api.entity.image.Time;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ThumbNailImagesDTO {

    private int id;
    private byte[] bytes;
    private Time time;

}
