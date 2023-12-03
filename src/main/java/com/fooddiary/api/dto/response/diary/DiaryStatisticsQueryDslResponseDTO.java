package com.fooddiary.api.dto.response.diary;

import com.fooddiary.api.entity.diary.DiaryTime;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class DiaryStatisticsQueryDslResponseDTO {
    private List<DiarySubStatistics> diarySubStatisticsList;
    private Long totalCount;

    @Getter
    @Setter
    @AllArgsConstructor
    public static class DiarySubStatistics {
        private DiaryTime diaryTime;
        private Long count;
    }
}
