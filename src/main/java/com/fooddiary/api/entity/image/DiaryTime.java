package com.fooddiary.api.entity.image;

import java.time.LocalDateTime;
import java.util.Arrays;

public enum DiaryTime {
    BREAKFAST("breakfast"), BRUNCH("brunch"),LUNCH("lunch"), SNACK("snack"),
    LINNER("linner"),DINNER("dinner"), LATESNACK("latesnack"), ETC("etc");

    private final String code;

    DiaryTime(String code) {
        this.code = code;
    }

    public static DiaryTime getTimeStatusByCode(final String code) {
        return Arrays.stream(values())
                .filter(diaryTime -> diaryTime.getCode().equals(code))
                .findFirst()
                .orElseThrow(() -> new RuntimeException("존재하지 않는 시간대입니다."));
    }

    public static DiaryTime getTime(final LocalDateTime dateTime) {
        final int hour = dateTime.getHour();
        final int minute = dateTime.getMinute();

        if (hour >= 21 || hour < 4) {
            return DiaryTime.LATESNACK;
        } else if (hour < 10) {
            return DiaryTime.BREAKFAST;
        } else if (hour < 12 && minute < 31) {
            return DiaryTime.BRUNCH;
        } else if (hour < 14) {
            return DiaryTime.LUNCH;
        } else if (hour < 16) {
            return DiaryTime.SNACK;
        } else if (hour < 18) {
            return DiaryTime.LINNER;
        } else {
            return DiaryTime.DINNER;
        }
    }

    public String getCode() {
        return code;
    }
}
