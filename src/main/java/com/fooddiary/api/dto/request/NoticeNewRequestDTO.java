package com.fooddiary.api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeNewRequestDTO {
    private String title;
    private String content;
    private boolean available;
}
