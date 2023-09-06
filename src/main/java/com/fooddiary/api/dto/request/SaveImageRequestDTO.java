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

    @JsonFormat(shape= JsonFormat.Shape.STRING, pattern="yyyy-MM-dd'T'HH:mm:ss", timezone = "Asia/Seoul")
    private LocalDateTime localDateTime;

    @Nullable
    private Double longitude;
    @Nullable
    private Double latitude;
}
