package com.fooddiary.api.dto.response;

import com.fooddiary.api.entity.image.DayImage;
import com.fooddiary.api.entity.image.Image;
import com.fooddiary.api.entity.image.Time;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.net.URL;
import java.util.List;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class DayImagesDto {

    byte[] bytes;
    Time time;

}
