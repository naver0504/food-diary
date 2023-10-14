package com.fooddiary.api.dto.request.notice;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeModifyRequestDTO extends NoticeNewRequestDTO {
    private Integer id;
}
