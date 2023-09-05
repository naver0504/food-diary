package com.fooddiary.api.dto.request;

import lombok.Getter;
import lombok.Setter;

import java.time.LocalDate;

@Getter
@Setter
public class NoticeNewRequestDTO {
    private String title;
    private String content;
    private LocalDate noticeAt;
    private boolean available;
}
