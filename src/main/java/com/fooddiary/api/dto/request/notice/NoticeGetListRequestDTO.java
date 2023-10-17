package com.fooddiary.api.dto.request.notice;

import org.springframework.data.domain.Pageable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NoticeGetListRequestDTO {
    private int startId;
    private int size;
}
