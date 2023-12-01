package com.fooddiary.api.repository.diary;

import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.diary.Time;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fooddiary.api.entity.diary.QDiary.diary;


@Repository
@Slf4j
public class DiaryQuerydslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public DiaryQuerydslRepository(EntityManager entityManager) {
        jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    public List<Diary> getTimeLineDayImage(final int year, final int month, final int startDay, final int userId) {
        return jpaQueryFactory.selectFrom(diary)
                .where(
                        diary.user.id.eq(userId)

                )
             //   .orderBy(diary.time.day.desc())
                .limit(4)
                .fetch();


    }

    public Time getTime(final int imageId) {
return null;

    }

    public Map<String, Time> getBeforeAndAfterTime(final int year, final int month, final int day, final int userId) {
        final Map<String, Time> timeMap = new HashMap<>();


        return timeMap;
    }

    public boolean existByUserId(final int userId) {
        Integer fetchFirst = jpaQueryFactory
                .selectOne()
                .from(diary)
                .where(diary.user.id.eq(userId))
                .fetchFirst();

        return fetchFirst != null;
    }
}
