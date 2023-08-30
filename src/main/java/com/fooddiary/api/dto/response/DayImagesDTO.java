package com.fooddiary.api.dto.response;

import com.fooddiary.api.entity.image.Time;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DayImagesDTO {

    int id;
    byte[] bytes;
    Time time;

}
