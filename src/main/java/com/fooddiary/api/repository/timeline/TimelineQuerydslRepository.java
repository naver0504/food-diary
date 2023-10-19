package com.fooddiary.api.repository.timeline;

import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.diary.Image;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

import static com.fooddiary.api.entity.diary.QDiary.diary;
import static com.fooddiary.api.entity.diary.QImage.image;
import static java.time.temporal.ChronoUnit.NANOS;

@Repository
@Slf4j
public class TimelineQuerydslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public TimelineQuerydslRepository(EntityManager entityManager) {
        jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    public List<Diary> getTimeLineDiary(final LocalDate date, final int userId) {
        return jpaQueryFactory.selectFrom(diary) // todo - 날짜 뽑아오기
                .where(
                        diary.user.id.eq(userId),
                        diary.time.createTime.between(date.atStartOfDay(),
                                date.plusMonths(1).withDayOfMonth(1).atStartOfDay().minus(1, NANOS))
                ).groupBy(diary.createAt)
                .orderBy(diary.time.day.desc())
                .limit(4)
                .fetch();
    }

    public List<Image> getMoreImage(final LocalDate date, final int startId, final int userId) {
        return jpaQueryFactory.selectFrom(image)
                .innerJoin(diary.images, image)
                .fetchJoin()
                .where(
                        diary.user.id.eq(userId),
                        diary.time.createTime.between(date.atStartOfDay(),
                                date.plusDays(1).withDayOfMonth(1).atStartOfDay().minus(1, NANOS)),
                        image.id.goe(startId)
                )
                .orderBy(diary.time.day.desc(), image.updateAt.desc())
                .limit(4)
                .fetch();
    }
}
