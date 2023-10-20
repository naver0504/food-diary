package com.fooddiary.api.dto.response.timeline;

import java.time.LocalDate;

import jakarta.persistence.criteria.CriteriaBuilder.In;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class TimelineDiaryDslQueryDTO {
    private String date;
}
