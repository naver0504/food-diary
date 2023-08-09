package com.fooddiary.api.dto.response;

import com.fooddiary.api.entity.image.DayImage;
import com.fooddiary.api.entity.image.Image;
import com.fooddiary.api.entity.image.Time;
import lombok.*;

import java.net.URL;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DayImagesDto {

    int id;
    byte[] bytes;
    Time time;

}
