package com.fooddiary.api.controller;

import org.springframework.data.domain.Pageable;
import org.springframework.web.bind.annotation.RestController;

import com.fooddiary.api.service.BoardService;

import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class BoardController {
    private final BoardService boardService;

    public void getBoard(Pageable pageable) {
        boardService.getBoard(pageable);

    }

}


