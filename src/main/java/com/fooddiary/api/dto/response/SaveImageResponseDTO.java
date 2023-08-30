package com.fooddiary.api.dto.response;


import lombok.*;

@Getter
@Setter
@ToString
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SaveImageResponseDTO {

    private Status status;

    public enum Status {
        SUCCESS
    }
}
