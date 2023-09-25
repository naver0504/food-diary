package com.fooddiary.api.entity.image;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Embeddable
@NoArgsConstructor
@ToString
public class Time {

    private int year;
    private int month;
    private int day;
    private LocalDateTime localDateTime;


    public Time(final LocalDateTime dateTime) {
        this.year = dateTime.getYear();
        this.month = dateTime.getMonth().getValue();
        this.day = dateTime.getDayOfMonth();
        this.localDateTime = dateTime;
    }
}
