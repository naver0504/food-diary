package com.fooddiary.api.entity.user;

import java.util.stream.Stream;

import javax.annotation.Nullable;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class StatusConverter implements AttributeConverter<Status, String> {

    @Override
    @Nullable
    public String convertToDatabaseColumn(Status status) {
        if (status == null) {
            return null;
        }
        return status.getCode();
    }

    @Override
    @Nullable
    public Status convertToEntityAttribute(String code) {
        if (code == null) {
            return null;
        }

        return Stream.of(Status.values())
                     .filter(c -> c.getCode().equals(code))
                     .findFirst()
                     .orElseThrow(IllegalArgumentException::new);
    }
}
