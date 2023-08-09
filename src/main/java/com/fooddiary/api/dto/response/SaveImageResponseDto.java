package com.fooddiary.api.dto.response;


import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveImageResponseDto {

    private Status status;

    public enum Status {
        SUCCESS
    }
}
