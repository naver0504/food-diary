package com.fooddiary.api.dto.response;


import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class SaveImageResponseDto {

    private Status status;

    public enum Status {
        SUCCESS
    }
}
