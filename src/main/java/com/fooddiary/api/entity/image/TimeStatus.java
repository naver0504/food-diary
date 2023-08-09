package com.fooddiary.api.entity.image;

import java.time.LocalDateTime;

public enum TimeStatus {
    BREAKFAST("아침"), BRUNCH("아점"),LUNCH("점심"), SNACK("간식"),
    LINNER("점저"),DINNER("저녁"), LATESNACK("야식");

    private final String code;

    TimeStatus(String code) {
        this.code = code;
    }

    public static TimeStatus getTime(final LocalDateTime dateTime) {
        final int hour = dateTime.getHour();
        final int minute = dateTime.getMinute();

        if (hour >= 21 || hour < 4) {
            return TimeStatus.LATESNACK;
        } else if (hour < 10) {
            return TimeStatus.BREAKFAST;
        } else if (hour < 12 && minute < 31) {
            return TimeStatus.BRUNCH;
        } else if (hour < 14) {
            return TimeStatus.LUNCH;
        } else if (hour < 16) {
            return TimeStatus.SNACK;
        } else if (hour < 18) {
            return TimeStatus.LINNER;
        } else {
            return TimeStatus.DINNER;
        }
    }

    public String getCode() {
        return code;
    }
}
