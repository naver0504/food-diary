package com.fooddiary.api.dto.response;

import java.time.LocalDate;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeResponseDTO {
    private Integer id;
    private String title;
    private String content;
    private LocalDate createAt;
}
