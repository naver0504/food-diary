package com.fooddiary.api.entity.diary;

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

    public static LocalDateTime getDateWithMinTime(final int year, final int month, final int day) {
        return LocalDateTime.of(year, month, day, 0, 0);
    }

    public static LocalDateTime getDateWithMaxTime(final int year, final int month, final int day) {
        return LocalDateTime.of(year, month, day, 23, 59, 59, 999999000); // mySQL의 기본값은 microseconds 이므로 6자리입니다.
    }

    public static String getDayOfWeek(final Time time) {
        int dayOfWeek = Time.getDateWithMinTime(time.getYear(), time.getMonth(), time.getDay())
                .getDayOfWeek().getValue();

        return switch (dayOfWeek) {
            case 1 -> "월";
            case 2 -> "화";
            case 3 -> "수";
            case 4 -> "목";
            case 5 -> "금";
            case 6 -> "토";
            case 7 -> "일";
            default -> throw new RuntimeException("요일을 찾을 수 없습니다.");
        };
    }

}
