package com.fooddiary.api.entity.user;

import java.util.stream.Stream;

import javax.annotation.Nullable;

import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class RoleConverter implements AttributeConverter<Role, String> {

    @Override
    @Nullable
    public String convertToDatabaseColumn(Role role) {
        if (role == null) {
            return null;
        }
        return role.getValue();
    }

    @Override
    @Nullable
    public Role convertToEntityAttribute(String value) {
        if (value == null) {
            return null;
        }

        return Stream.of(Role.values())
                     .filter(c -> c.getValue().equals(value))
                     .findFirst()
                     .orElseThrow(IllegalArgumentException::new);
    }
}
