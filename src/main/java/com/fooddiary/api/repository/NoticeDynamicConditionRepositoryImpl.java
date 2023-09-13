package com.fooddiary.api.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

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
public class NoticeDynamicConditionRepositoryImpl implements NoticeDynamicConditionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    private static <T> List<Predicate> orCondition(CriteriaBuilder cb, Root<T> notice, String title,
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

    private static <T> List<Predicate> andCondition(CriteriaBuilder cb, Root<T> notice, Boolean available,
                                                    LocalDate noticeAtStart, LocalDate noticeAtEnd) {
        final List<Predicate> predicateList = new ArrayList<>();
        if (available != null) {
            predicateList.add(cb.equal(notice.get("available"), available));
        }
        if (noticeAtStart != null && noticeAtEnd != null) {
            predicateList.add(cb.between(notice.get("noticeAt"), noticeAtStart, noticeAtEnd));
        }
        return predicateList;
    }

    @Override
    public long selectCount(String title, String content, Boolean available, LocalDate noticeAtStart,
                            LocalDate noticeAtEnd) {
        final CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        final CriteriaQuery<Long> query = cb.createQuery(Long.class);
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

        return entityManager.createQuery(query).getResultList();
    }

}
