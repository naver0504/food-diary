package com.fooddiary.api.dto.request;

import org.springframework.data.domain.Pageable;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BoardRequestDTO {
    private Pageable page;
}
