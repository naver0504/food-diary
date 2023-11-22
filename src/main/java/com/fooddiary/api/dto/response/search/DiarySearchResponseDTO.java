package com.fooddiary.api.dto.response.search;

import com.fooddiary.api.dto.request.search.CategoryType;
import com.fooddiary.api.dto.response.timeline.TimelineDiaryDTO;
import lombok.*;

import java.util.List;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DiarySearchResponseDTO {

    private String categoryName;
    private CategoryType categoryType;

    private int count;
    private List<TimelineDiaryDTO> diaryList;
}
