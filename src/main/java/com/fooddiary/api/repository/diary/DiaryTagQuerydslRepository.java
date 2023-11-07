package com.fooddiary.api.repository.diary;

import com.fooddiary.api.entity.diary.QDiaryTag;
import com.fooddiary.api.entity.diary.QTag;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import static com.fooddiary.api.entity.diary.QDiary.*;
import static com.fooddiary.api.entity.diary.QDiaryTag.*;
import static com.fooddiary.api.entity.diary.QTag.*;

@Repository
@Slf4j
public class DiaryTagQuerydslRepository {

    private final JPAQueryFactory jpaQueryFactory;


    public DiaryTagQuerydslRepository(EntityManager entityManager) {
        jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    public boolean existByUserId(final int userId) {
        Integer result = jpaQueryFactory.selectOne()
                .from(QDiaryTag.diaryTag)
                .innerJoin(QDiaryTag.diaryTag.diary, diary)
                .where(diary.user.id.eq(userId))
                .fetchFirst();

        return result != null;
    }

    public boolean existByUserIdAndTagName(final int userId, final String tagName) {
        Integer result = jpaQueryFactory.selectOne()
                .from(diaryTag)
                .innerJoin(diaryTag.diary, diary)
                .innerJoin(diaryTag.tag, tag)
                .where(diary.user.id.eq(userId), tag.tagName.eq(tagName))
                .fetchFirst();

        return result != null;
    }
}
