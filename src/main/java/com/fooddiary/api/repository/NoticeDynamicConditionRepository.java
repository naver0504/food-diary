package com.fooddiary.api.repository;

import java.time.LocalDate;
import java.util.List;

import org.springframework.data.domain.Pageable;

import com.fooddiary.api.entity.notice.Notice;

public interface NoticeDynamicConditionRepository {
    long selectCount(String title, String content, Boolean available, LocalDate noticeAtStart, LocalDate noticeAtEnd);

    List<Notice> selectList(String title, String content, Boolean available, LocalDate noticeAtStart, LocalDate noticeAtEnd,
                                   Pageable pageable);
}
