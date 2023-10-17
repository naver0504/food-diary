package com.fooddiary.api.repository.notice;

import com.fooddiary.api.entity.notice.Notice;
import com.fooddiary.api.entity.notice.QNotice;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public class NoticeQuerydslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public NoticeQuerydslRepository(EntityManager entityManager) {
        jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    private static BooleanBuilder orCondition(String title, String content) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (title != null) {
            booleanBuilder.or(QNotice.notice.title.like('%' + title + '%'));
        }
        if (content != null) {
            booleanBuilder.or(QNotice.notice.content.like('%' + title + '%'));
        }
        return booleanBuilder;
    }

    private static BooleanBuilder andCondition(Boolean available,
                                                            LocalDate noticeAtStart, LocalDate noticeAtEnd) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        if (available != null) {
            booleanBuilder.and(QNotice.notice.available.eq(available));
        }
        if (noticeAtStart != null && noticeAtEnd != null) {
            booleanBuilder.and(QNotice.notice.noticeAt.between(noticeAtStart, noticeAtEnd));
        }
        return booleanBuilder;
    }


    public long selectCount(String title, String content, Boolean available, LocalDate noticeAtStart,
                            LocalDate noticeAtEnd) {
        return jpaQueryFactory.select(QNotice.notice)
                              .from(QNotice.notice)
                              .where(orCondition(title, content), andCondition(available, noticeAtStart, noticeAtEnd))
                              .stream().count();
    }


    public List<Notice> selectList(String title, String content, Boolean available, LocalDate noticeAtStart,
                                   LocalDate noticeAtEnd,
                                   Pageable pageable) {
        return jpaQueryFactory.select(QNotice.notice)
                              .from(QNotice.notice)
                              .where(orCondition(title, content), andCondition(available, noticeAtStart, noticeAtEnd))
                              .limit(pageable.getPageSize())
                              .offset(pageable.getOffset())
                              .stream().toList();
    }

}
