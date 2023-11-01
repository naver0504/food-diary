package com.fooddiary.api.repository.diary;

import com.fooddiary.api.entity.diary.QDiaryTag;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import static com.fooddiary.api.entity.diary.QDiary.*;

@Repository
@Slf4j
public class DiaryTagQuerydslRepository {

    private final JPAQueryFactory jpaQueryFactory;


    public DiaryTagQuerydslRepository(EntityManager entityManager) {
        jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    public boolean existByUserId(final int userId) {
        Integer diaryTag = jpaQueryFactory.selectOne()
                .from(QDiaryTag.diaryTag)
                .join(QDiaryTag.diaryTag.diary, diary)
                .where(diary.user.id.eq(userId))
                .fetchFirst();

        return diaryTag != null;
    }
}
