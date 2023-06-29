package com.fooddiary.api.entity.image;

import jakarta.persistence.Embeddable;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.time.LocalDateTime;

@Getter
@Embeddable
@NoArgsConstructor
@ToString
public class Time {

    private String year;
    private String month;
    private String day;


    public Time(LocalDateTime dateTime) {
        this.year = String.valueOf(dateTime.getYear());
        this.month = String.valueOf(dateTime.getMonth());
        this.day = String.valueOf(dateTime.getDayOfMonth());
    }
}
