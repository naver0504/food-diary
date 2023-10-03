package com.fooddiary.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class UpdateImageDetailDTO {

    private String memo;
    private String timeStatus;
    private List<String> tags;
}
