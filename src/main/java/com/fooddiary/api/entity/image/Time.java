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

    private int year;
    private int month;
    private int day;


    public Time(LocalDateTime dateTime) {
        this.year = dateTime.getYear();
        this.month = dateTime.getMonth().getValue();
        this.day = dateTime.getDayOfMonth();
    }
}
