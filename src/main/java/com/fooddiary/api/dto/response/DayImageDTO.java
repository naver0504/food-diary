package com.fooddiary.api.dto.response;


import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class DayImageDTO {

    int id;
    byte[] bytes;
    String timeStatus;
}
