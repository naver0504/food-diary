package com.fooddiary.api.common.filter;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class ResponseLogDTO {
    private String response;
    private Long timeLap;
}
