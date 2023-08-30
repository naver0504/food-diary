package com.fooddiary.api.repository;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;

import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.entity.board.Board;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles(Profiles.TEST)
class BoardRepositoryTest {
    @Autowired
    BoardRepository boardRepository;

    @Test
    void selectBoardByIdAndPaging() {
        final List<Board> saveList = new ArrayList<>();
        Board board = new Board();
        board.setTitle("신규 출시");
        board.setContent("2023년도부터 개발하여 2024년도에 드디에 앱을 출시했습니다. 많은 이용 부탁드립니다.");
        saveList.add(board);

        board = new Board();
        board.setTitle("긴급패치");
        board.setContent("로그인 후 사진조회가 안되는 문제 수정");
        boardRepository.saveAll(saveList);

        final List<Board> boardList = boardRepository.selectBoardByIdPaging(1, PageRequest.of(0, 2));

        Assertions.assertEquals(boardList.size(), 2);
    }
}
