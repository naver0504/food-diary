package com.fooddiary.api.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fooddiary.api.entity.board.Board;

public interface BoardRepository extends JpaRepository<Board, Integer> {
    @Query("select b from Board b where b.id >= :id and b.show=1 order by b.id desc")
    List<Board> selectBoardByIdPaging(@Param("id")Integer id, Pageable pageable);
}
