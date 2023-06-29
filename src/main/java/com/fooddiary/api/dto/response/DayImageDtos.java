package com.fooddiary.api.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@AllArgsConstructor
public class DayImageDtos {

    List<DayImageDto> dayImageDtoList;
}
