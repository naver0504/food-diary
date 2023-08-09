package com.fooddiary.api.dto.response;


import com.fooddiary.api.entity.image.TimeStatus;
import lombok.*;

import java.net.URL;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DayImageDto {

    int id;
    byte[] bytes;
    String timeStatus;
}
