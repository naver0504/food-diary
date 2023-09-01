package com.fooddiary.api.dto.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeModifyRequestDTO extends NoticeNewRequestDTO {
    private Integer id;
}
