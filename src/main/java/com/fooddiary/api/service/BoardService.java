package com.fooddiary.api.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.fooddiary.api.entity.board.Board;
import com.fooddiary.api.repository.BoardRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BoardService {
    private final BoardRepository boardRepository;
    public void getBoard(Pageable pageable) {
        final int startId = (int) pageable.getOffset();
        pageable = PageRequest.of(0, pageable.getPageSize());
        final List<Board> boardList = boardRepository.selectBoardByIdPaging(startId, pageable);
    }
}
