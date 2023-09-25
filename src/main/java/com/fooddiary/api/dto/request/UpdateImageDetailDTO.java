package com.fooddiary.api.dto.request;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@AllArgsConstructor
@NoArgsConstructor
public class UpdateImageDetailDTO {

    private String memo;
    private String timeStatus;
    private List<String> tags;
}
