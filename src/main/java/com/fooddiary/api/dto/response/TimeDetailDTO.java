package com.fooddiary.api.dto.response;

import com.fooddiary.api.entity.image.Time;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class TimeDetailDTO {

    private int month;
    private int day;
    private String dayOfWeek;

    public static TimeDetailDTO of(final Time time) {
        return TimeDetailDTO.builder()
                .month(time.getMonth())
                .day(time.getDay())
                .dayOfWeek(Time.getDayOfWeek(time))
                .build();
    }
}
