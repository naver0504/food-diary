package com.fooddiary.api.repository;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Predicate;
import jakarta.persistence.criteria.Root;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import com.fooddiary.api.entity.notice.Notice;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import jakarta.persistence.TypedQuery;

@Repository
public class NoticeDynamicConditionRepositoryImpl implements NoticeDynamicConditionRepository {

    @PersistenceContext
    private EntityManager entityManager;

    @Override
    public long selectCount(String title, String content, Boolean available, LocalDate noticeAt) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Long> query = cb.createQuery(Long.class);
        Root<Notice> notice = query.from(Notice.class);
        List<Predicate> predicateOrList = orCondition(cb, notice, title, content);
        List<Predicate> predicateAndList = andCondition(cb, notice, available, noticeAt);
        List<Predicate> predicateList = new ArrayList<>();
        if (predicateOrList.size() > 0) {
            predicateList.add(cb.or(predicateOrList.toArray(new Predicate[predicateOrList.size()])));
        }
        if (predicateAndList.size() > 0) {
            predicateList.add(cb.and(predicateAndList.toArray(new Predicate[predicateAndList.size()])));
        }
        Predicate[] condition = new Predicate[predicateList.size()];
        for(int i=0;i < condition.length; i++) condition[i] = predicateList.get(i);
        query.select(cb.count(notice))
                .where(condition);
        return entityManager.createQuery(query).getSingleResult();

/*
        final TypedQuery<Long> jpaQuery = entityManager.createQuery(
                jpqlPrefix(true, title, content, available, noticeAt), Long.class);
        setParam(jpaQuery, title, content, available, noticeAt);
        return jpaQuery.getSingleResult();

 */
    }

    private <T> List<Predicate> orCondition (CriteriaBuilder cb, Root<T> notice, String title, String content) {
        List<Predicate> predicateList = new ArrayList<>();
        if (title != null) {
            predicateList.add(cb.like(notice.get("title"), "%" + title + "%"));
        }
        if (content != null) {
            predicateList.add(cb.like(notice.get("content"), "%" + content + "%"));
        }
        return predicateList;
    }

    private <T> List<Predicate> andCondition(CriteriaBuilder cb, Root<T> notice, Boolean available, LocalDate noticeAt) {
        List<Predicate> predicateList = new ArrayList<>();
        if (available != null) {
            predicateList.add(cb.equal(notice.get("available"), available));
        }
        if (noticeAt != null) {
            predicateList.add(cb.equal(notice.get("noticeAt"), noticeAt));
        }
        return predicateList;
    }

    @Override
    public List<Notice> selectList(String title, String content, Boolean available, LocalDate noticeAt,
                                   Pageable pageable) {
        CriteriaBuilder cb = entityManager.getCriteriaBuilder();
        CriteriaQuery<Notice> query = cb.createQuery(Notice.class);
        Root<Notice> notice = query.from(Notice.class);
        List<Predicate> predicateOrList = orCondition(cb, notice, title, content);
        List<Predicate> predicateAndList = andCondition(cb, notice, available, noticeAt);
        List<Predicate> predicateList = new ArrayList<>();
        if (predicateOrList.size() > 0) {
            predicateList.add(cb.or(predicateOrList.toArray(new Predicate[predicateOrList.size()])));
        }
        if (predicateAndList.size() > 0) {
            predicateList.add(cb.and(predicateAndList.toArray(new Predicate[predicateAndList.size()])));
        }
        Predicate[] condition = new Predicate[predicateList.size()];
        for(int i=0;i < condition.length; i++) condition[i] = predicateList.get(i);
        query.select(notice)
                .where(condition);

        return entityManager.createQuery(query).getResultList();
        /*
        final TypedQuery<Notice> jpaQuery = entityManager.createQuery(
                jpqlPrefix(false, title, content, available, noticeAt), Notice.class);
        setParam(jpaQuery, title, content, available, noticeAt);

        return jpaQuery.setFirstResult((int) pageable.getOffset())
                       .setMaxResults(pageable.getPageSize() + 1)
                       .getResultList();
*/
    }

    private static String jpqlPrefix(boolean isCount, String title, String content, Boolean available,
                              LocalDate noticeAt) {
        String jpql = isCount ? "select count(*) from Notice n" : "select n from Notice n";
        final String order = " order by n.noticeAt desc";
        final String where = " where ";
        final List<String> whereCondition = new LinkedList<>();

        // string 동적생성
        if (title != null) {
            whereCondition.add("n.title like :title");
        }
        if (content != null) {
            whereCondition.add("n.content like :content");
        }
        if (available != null) {
            whereCondition.add("n.available = :available");
        }
        if (noticeAt != null) {
            whereCondition.add("n.noticeAt = :noticeAt");
        }

        if (!whereCondition.isEmpty()) {
            jpql += where + String.join(" and ", whereCondition);
        }
        jpql += order;

        return jpql;
    }

    private static <T> void setParam(TypedQuery<T> jpaQuery, String title, String content, Boolean available,
                              LocalDate noticeAt) {
        // param 동적 생성
        if (title != null) {
            jpaQuery.setParameter("title", "%" + title + "%");
        }
        if (content != null) {
            jpaQuery.setParameter("content", content);
        }
        if (available != null) {
            jpaQuery.setParameter("available", available);
        }
        if (noticeAt != null) {
            jpaQuery.setParameter("noticeAt", noticeAt);
        }
    }
}
