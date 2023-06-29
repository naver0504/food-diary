package com.fooddiary.api.entity.image;

import java.time.LocalDateTime;

public enum TimeStatus {
    MORNING("아침"), BRUNCH("아점"),LUNCH("점심"), SNACK("간식"),
    LINNER("점저"),DINNER("저녁"), LATESNACK("야식");

    private final String code;

    TimeStatus(String code) {
        this.code = code;
    }

    public static TimeStatus getTime(LocalDateTime dateTime) {
        int hour = dateTime.getHour();
        int minute = dateTime.getMinute();

        if (hour >= 4 && hour <= 10) {
            return TimeStatus.MORNING;
        } else if (hour >= 10 && hour <= 11 && minute<= 30) {
            return TimeStatus.BRUNCH;
        } else if (hour >= 11 && hour <= 14) {
            return TimeStatus.LUNCH;
        } else if (hour >= 14 && hour <= 16) {
            return TimeStatus.SNACK;
        } else if (hour >= 16 && hour <= 18) {
            return TimeStatus.LINNER;
        } else if (hour >= 18 && hour <= 21) {
            return TimeStatus.DINNER;
        } else {
            return TimeStatus.LATESNACK;
        }
    }

    public String getCode() {
        return code;
    }
}
