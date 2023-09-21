package com.fooddiary.api.repository;

import java.time.LocalDate;
import java.util.List;

import org.jetbrains.annotations.Nullable;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.fooddiary.api.entity.notice.Notice;
import com.fooddiary.api.entity.notice.QNotice;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;

@Repository
public class NoticeDynamicConditionRepositoryImpl implements NoticeDynamicConditionRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public NoticeDynamicConditionRepositoryImpl(EntityManager entityManager) {
        jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    private static @Nullable BooleanExpression orCondition(String title, String content) {
        BooleanExpression booleanExpression = null;
        if (title != null) {
            booleanExpression = QNotice.notice.title.like('%' + title + '%');
        }
        if (content != null) {
            if (booleanExpression != null) {
                booleanExpression = booleanExpression.or(QNotice.notice.content.like('%' + title + '%'));
            } else {
                booleanExpression = QNotice.notice.content.like('%' + title + '%');
            }

        }
        return booleanExpression;
    }

    private static @Nullable BooleanExpression andCondition(Boolean available,
                                                            LocalDate noticeAtStart, LocalDate noticeAtEnd) {
        BooleanExpression booleanExpression = null;
        if (available != null) {
            booleanExpression = QNotice.notice.available.eq(available);
        }
        if (noticeAtStart != null && noticeAtEnd != null) {
            if (booleanExpression != null) {
                booleanExpression = booleanExpression.and(
                        QNotice.notice.noticeAt.between(noticeAtStart, noticeAtEnd));
            } else {
                booleanExpression = QNotice.notice.noticeAt.between(noticeAtStart, noticeAtEnd);
            }
        }
        return booleanExpression;
    }

    @Override
    public long selectCount(String title, String content, Boolean available, LocalDate noticeAtStart,
                            LocalDate noticeAtEnd) {
        final BooleanExpression predicateOrList = orCondition(title, content);
        final BooleanExpression predicateAndList = andCondition(available, noticeAtStart, noticeAtEnd);
        BooleanExpression condition = Expressions.asBoolean(true).isTrue();
        if (predicateOrList != null) {
            condition = condition.and(predicateOrList);
        }
        if (predicateAndList != null) {
            condition = condition.and(predicateAndList);
        }
        return jpaQueryFactory.select(QNotice.notice).from(QNotice.notice).where(condition).stream().count();
    }

    @Override
    public List<Notice> selectList(String title, String content, Boolean available, LocalDate noticeAtStart,
                                   LocalDate noticeAtEnd,
                                   Pageable pageable) {
        final BooleanExpression predicateOrList = orCondition(title, content);
        final BooleanExpression predicateAndList = andCondition(available, noticeAtStart, noticeAtEnd);
        BooleanExpression condition = Expressions.asBoolean(true).isTrue();
        if (predicateOrList != null) {
            condition = condition.and(predicateOrList);
        }
        if (predicateAndList != null) {
            condition = condition.and(predicateAndList);
        }

        return jpaQueryFactory.select(QNotice.notice).from(QNotice.notice).where(condition).stream().toList();
    }

}
