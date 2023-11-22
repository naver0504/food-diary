package com.fooddiary.api.dto.request.search;

import org.springframework.core.convert.converter.Converter;

public class CategoryTypeConverter implements Converter<String, CategoryType>{

    @Override
    public CategoryType convert(String source) {
        return CategoryType.fromString(source.toUpperCase());
    }
}
