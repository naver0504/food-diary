package com.fooddiary.api.dto.request.diary;

import org.springframework.core.convert.converter.Converter;

import com.fooddiary.api.entity.diary.DiaryTime;

public class DiaryTimeConverter implements Converter<String, DiaryTime> {
    @Override
    public DiaryTime convert(String source) {
        return DiaryTime.valueOf(source.toUpperCase());
    }
}
