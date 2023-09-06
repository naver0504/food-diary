package com.fooddiary.api.dto.request;


import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class SaveImageRequestDTO {

    private LocalDateTime localDateTime;
    private Double longitude;
    private Double latitude;
}
