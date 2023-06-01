package com.fooddiary.api.entity;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

import java.util.stream.Stream;

@Converter(autoApply = true)
public class CreatePathConverter implements AttributeConverter<CreatePath, String> {

    @Override
    public String convertToDatabaseColumn(CreatePath category) {
        if (category == null) {
            return null;
        }
        return category.getCode();
    }

    @Override
    public CreatePath convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }

        return Stream.of(CreatePath.values())
                .filter(c -> c.getCode().equals(code))
                .findFirst()
                .orElseThrow(IllegalArgumentException::new);
    }
}
