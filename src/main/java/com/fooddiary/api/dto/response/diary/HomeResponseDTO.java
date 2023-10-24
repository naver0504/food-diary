package com.fooddiary.api.dto.response.diary;

import java.time.LocalDate;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomeResponseDTO {
    private long id; // diary id
    private byte[] bytes;
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd")
    private LocalDate time;
}
