package com.fooddiary.api.entity.image;

import jakarta.persistence.Embeddable;
import lombok.*;

import java.time.LocalDateTime;


@Getter
@Embeddable
@NoArgsConstructor
public class Time {

    private int year;
    private int month;
    private int day;
    private LocalDateTime createTime;


    public Time(final LocalDateTime dateTime) {
        this.year = dateTime.getYear();
        this.month = dateTime.getMonth().getValue();
        this.day = dateTime.getDayOfMonth();
        this.createTime = dateTime;
    }

    public static LocalDateTime getDateTime(final int year, final int month, final int day) {
        return LocalDateTime.of(year, month, day, 0, 0);
    }
    public static String getDayOfWeek(final Time time) {
        int dayOfWeek = Time.getDateTime(time.getYear(), time.getMonth(), time.getDay())
                .getDayOfWeek().getValue();

        switch (dayOfWeek) {
            case 1:
                return "월";
            case 2:
                return "화";
            case 3:
                return "수";
            case 4:
                return "목";
            case 5:
                return "금";
            case 6:
                return "토";
            case 7:
                return "일";
            default:
                throw new RuntimeException("요일을 찾을 수 없습니다.");
        }
    }

}
