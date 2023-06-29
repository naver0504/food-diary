package com.fooddiary.api.dto.response;


import com.fooddiary.api.entity.image.TimeStatus;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.net.URL;

@Getter
@Setter
@AllArgsConstructor
public class DayImageDto {

    URL url;
    String timeStatus;
}
