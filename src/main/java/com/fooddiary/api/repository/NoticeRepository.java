package com.fooddiary.api.repository;

import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.fooddiary.api.entity.notice.Notice;

public interface NoticeRepository extends JpaRepository<Notice, Integer> {
    @Query("select n from Notice n where n.id >= :id and n.available=:available and n.noticeAt <= CURRENT_DATE order by n.id desc")
    List<Notice> selectMoreNoticeListById(@Param("id")Integer id, @Param("available")boolean available, Pageable pageable);

    @Query("select n from Notice n order by n.id desc")
    List<Notice> selectPagingNoticeListById(Pageable pageable);
}
