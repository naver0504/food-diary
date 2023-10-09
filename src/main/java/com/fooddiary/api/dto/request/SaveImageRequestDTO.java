package com.fooddiary.api.dto.request;


import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.annotation.Nullable;
import lombok.*;

import java.time.LocalDateTime;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class SaveImageRequestDTO {

    private LocalDateTime localDateTime;
    private Double longitude = -200D;
    private Double latitude = -200D;
}
