package com.fooddiary.api.dto.request.search;


import java.util.Arrays;

public enum CategoryType {

    DIARY_TIME,
    PLACE,
    TAG,
    ETC;

    public static CategoryType fromString(String value) {
        return Arrays.stream(CategoryType.values())
                .filter(categoryType -> categoryType.name().equals(value))
                .findFirst()
                .orElse(CategoryType.ETC);
    }

}
