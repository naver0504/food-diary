package com.fooddiary.api.dto.response.timeline;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class TimelineDiaryDTO {
    private Integer diaryId;
    private byte[] bytes;
}
