package com.fooddiary.api.dto.request;


import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SaveImageRequestDTO {

    private LocalDateTime localDateTime;
    private Double longitude;
    private Double latitude;
}
