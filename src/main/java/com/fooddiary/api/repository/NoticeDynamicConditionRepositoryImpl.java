package com.fooddiary.api.repository;

import java.time.LocalDate;
import java.util.LinkedList;
import java.util.List;

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
    public int selectCount(String title, String content, Boolean available, LocalDate noticeAt) {
        final TypedQuery<Integer> jpaQuery = entityManager.createQuery(
                jpqlPrefix(true, title, content, available, noticeAt), Integer.class);
        setParam(jpaQuery, title, content, available, noticeAt);
        return jpaQuery.getSingleResult();
    }

    @Override
    public List<Notice> selectList(String title, String content, Boolean available, LocalDate noticeAt,
                                   Pageable pageable) {
        final TypedQuery<Notice> jpaQuery = entityManager.createQuery(
                jpqlPrefix(true, title, content, available, noticeAt), Notice.class);
        setParam(jpaQuery, title, content, available, noticeAt);

        return jpaQuery.setFirstResult((int) pageable.getOffset())
                       .setMaxResults(pageable.getPageSize() + 1)
                       .getResultList();

    }

    private static String jpqlPrefix(boolean isCount, String title, String content, Boolean available,
                              LocalDate noticeAt) {
        String jpql = isCount ? "select count(*) from Notice n" : "select n from Notice n";
        final String order = " order by n.noticeAt desc";
        final String where = " where ";
        final List<String> whereCondition = new LinkedList<>();

        // string 동적생성
        if (title != null) {
            whereCondition.add("n.title like concat('%%',:title,'%%')");
        }
        if (content != null) {
            whereCondition.add("n.content like concat('%%',:content,'%%')");
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
            jpaQuery.setParameter("title", title);
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
