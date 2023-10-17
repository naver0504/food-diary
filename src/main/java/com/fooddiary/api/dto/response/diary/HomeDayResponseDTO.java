package com.fooddiary.api.dto.response.diary;


import com.fooddiary.api.dto.response.image.ImageResponseDTO;
import com.fooddiary.api.entity.image.DiaryTime;
import lombok.*;

import java.time.LocalDate;
import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class HomeDayResponseDTO {
    private LocalDate beforeDay;
    private LocalDate afterDay;
    private List<HomeDay> homeDayList;

    @Builder
    @Getter
    public static class HomeDay {
        private Integer id; // diary id
        private String memo;
        private DiaryTime diaryTime;
        private List<String> tags;
        private ImageResponseDTO image;
    }

}
