package com.fooddiary.api.repository.timeline;

import com.fooddiary.api.entity.diary.Diary;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.fooddiary.api.entity.diary.QDiary.diary;

@Repository
@Slf4j
public class TimelineQuerydslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public TimelineQuerydslRepository(EntityManager entityManager) {
        jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    public List<Diary> getTimeLineDayImage(final int year, final int month, final int startDay, final int userId) {
        return jpaQueryFactory.selectFrom(diary)
                .where(
                        diary.user.id.eq(userId),
                        diary.time.year.eq(year),
                        diary.time.month.eq(month),
                        diary.time.day.gt(startDay)
                )
                .orderBy(diary.time.day.desc())
                .limit(4)
                .fetch();


    }
}
