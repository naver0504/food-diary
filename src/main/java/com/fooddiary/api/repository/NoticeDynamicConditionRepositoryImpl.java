package com.fooddiary.api.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

import com.fooddiary.api.entity.notice.QNotice;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.fooddiary.api.entity.notice.Notice;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;

@Repository
@RequiredArgsConstructor
public class NoticeDynamicConditionRepositoryImpl implements NoticeDynamicConditionRepository {

    private final JPAQueryFactory jpaQueryFactory;

    private static BooleanExpression orCondition(String title, String content) {
        BooleanExpression booleanExpression = null;
        if (title != null) {
            booleanExpression = QNotice.notice.title.like( '%' + title + '%');
        }
        if (content != null) {
            if (booleanExpression != null) {
                booleanExpression = booleanExpression.or(QNotice.notice.content.like( '%' + title + '%'));
            } else {
                booleanExpression = QNotice.notice.content.like( '%' + title + '%');
            }

        }
        return booleanExpression;
    }

    private static <T> List<Predicate> orCondition2(CriteriaBuilder cb, Root<T> notice, String title,
                                                   String content) {
        final List<Predicate> predicateList = new ArrayList<>();
        if (title != null) {
            predicateList.add(cb.like(notice.get("title"), '%' + title + '%'));
        }
        if (content != null) {
            predicateList.add(cb.like(notice.get("content"), '%' + content + '%'));
        }
        return predicateList;
    }

    private static BooleanExpression andCondition(Boolean available,
                                                    LocalDate noticeAtStart, LocalDate noticeAtEnd) {
        BooleanExpression booleanExpression = null;
        if (available != null) {
            booleanExpression = QNotice.notice.available.eq(available);
        }
        if (noticeAtStart != null && noticeAtEnd != null) {
            if (booleanExpression != null) {
                booleanExpression = booleanExpression.and(QNotice.notice.noticeAt.between(noticeAtStart, noticeAtEnd));
            } else {
                booleanExpression = QNotice.notice.noticeAt.between(noticeAtStart, noticeAtEnd);
            }
        }
        return booleanExpression;
    }

    public long selectCount2(String title, String content, Boolean available, LocalDate noticeAtStart,
                             LocalDate noticeAtEnd) {
        jpaQueryFactory.select(QNotice.notice).where(orCondition(title, content).and(andCondition(available, noticeAtStart,
                noticeAtEnd)));

    }
    @Override
    public long selectCount(String title, String content, Boolean available, LocalDate noticeAtStart,
                            LocalDate noticeAtEnd) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> query = cb.createQuery(Long.class);
        final Root<Notice> notice = query.from(Notice.class);
        final BooleanExpression predicateOrList = orCondition(title, content);
        final BooleanExpression predicateAndList = andCondition(available, noticeAtStart, noticeAtEnd);
        BooleanExpression b = Expressions.asBoolean(true).isTrue();
        BooleanExpression unionCondition = new BooleanBuilder().and();
        final List<Predicate> predicateList = new ArrayList<>();
        if (predicateOrList != null) {
            b.and(predicateOrList);
        }
        if (predicateAndList != null) {
            b.and(predicateAndList);
        }
        final Predicate[] condition = new Predicate[predicateList.size()];
        for (int i = 0; i < condition.length; i++) {
            condition[i] = predicateList.get(i);
        }
        query.select(cb.count(notice))
             .where(condition);
        return entityManager.createQuery(query).getSingleResult();
    }

    @Override
    public List<Notice> selectList(String title, String content, Boolean available, LocalDate noticeAtStart,
                                   LocalDate noticeAtEnd,
                                   Pageable pageable) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Notice> query = cb.createQuery(Notice.class);
        final Root<Notice> notice = query.from(Notice.class);
        final List<Predicate> predicateOrList = orCondition(cb, notice, title, content);
        final List<Predicate> predicateAndList = andCondition(cb, notice, available, noticeAtStart,
                                                              noticeAtEnd);
        final List<Predicate> predicateList = new ArrayList<>();
        if (!predicateOrList.isEmpty()) {
            predicateList.add(cb.or(predicateOrList.toArray(new Predicate[predicateOrList.size()])));
        }
        if (!predicateAndList.isEmpty()) {
            predicateList.add(cb.and(predicateAndList.toArray(new Predicate[predicateAndList.size()])));
        }
        final Predicate[] condition = new Predicate[predicateList.size()];
        for (int i = 0; i < condition.length; i++) {
            condition[i] = predicateList.get(i);
        }
        query.select(notice)
             .where(condition);

        return entityManager.createQuery(query).setFirstResult((int) pageable.getOffset())
                            .setMaxResults(pageable.getPageSize()).getResultList();
    }

}
