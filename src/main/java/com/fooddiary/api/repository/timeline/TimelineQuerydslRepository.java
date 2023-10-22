package com.fooddiary.api.repository.timeline;

import com.fooddiary.api.dto.response.timeline.TimelineDiaryDslQueryDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.diary.Image;
import com.querydsl.core.types.ConstantImpl;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.DateTemplate;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringTemplate;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
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

    public List<TimelineDiaryDslQueryDTO> getTimeLineDate(final LocalDate date, final int userId) {
        DateTemplate dateFormat = Expressions.dateTemplate(String.class, "DATE_FORMAT({0},{1})", diary.createTime,
                                                           ConstantImpl.create("%Y-%m-%d"));
        return jpaQueryFactory.select(Projections.fields(TimelineDiaryDslQueryDTO.class, dateFormat.as("date")))
                              .from(diary)
                              .where(
                        diary.user.id.eq(userId),
                        diary.createTime.between(date.atStartOfDay(),
                                date.plusMonths(1).withDayOfMonth(1).atStartOfDay().minus(1, NANOS))
                ).groupBy(dateFormat)
                              .orderBy(dateFormat.asc())
                              .limit(4)
                              .fetch();

        /*
        return jpaQueryFactory.selectFrom(diary)
                              .innerJoin(image).fetchJoin()
                              .where(diary.user.id.eq(userId),
                                     diary.createTime.between(LocalDate.parse(dateList.get(0).getDate(), DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay(),
                                                              LocalDate.parse(dateList.get(dateList.size()-1).getDate(), DateTimeFormatter.ISO_LOCAL_DATE).atStartOfDay().plusDays(1).minusNanos(1L)))
                              .orderBy(diary.createTime.asc(), image.updateAt.desc())
                              .fetch();

         */
    }

    /**
     * 선택한 날짜에 있는 일기들을 불러옵니다.
     * @param date 선택한 날짜
     * @param startId 시작 일기 id
     * @param userId
     * @return
     */
    public List<Diary> getMoreDiary(final LocalDate date, final int startId, final int userId) {
        return jpaQueryFactory.selectFrom(diary)
                .innerJoin(image).fetchJoin()
                .where(
                        diary.user.id.eq(userId),
                        diary.createTime.between(date.atStartOfDay(),
                                date.plusDays(1).atStartOfDay().minus(1, NANOS)),
                        diary.id.goe(startId)
                )
                .orderBy(diary.createTime.desc(), image.updateAt.desc())
                .limit(4)
                .fetch();
    }
}
