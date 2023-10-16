package com.fooddiary.api.dto.response.diary;

import com.fooddiary.api.entity.diary.Time;
import lombok.*;

@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class HomeResponseDTO {
    private int id; // diary id
    private byte[] bytes;
    private Time time;
}
