package com.fooddiary.api.repository.diary;

import com.fooddiary.api.dto.response.diary.DiaryStatisticsQueryDslResponseDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.diary.DiaryTime;
import com.fooddiary.api.entity.diary.Time;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

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

    public Map<String, LocalDateTime> getBeforeAndAfterTime(final int year, final int month, final int day, final int userId) {
        final Map<String, LocalDateTime> timeMap = new HashMap<>();

        final LocalDateTime beforeTime = jpaQueryFactory.select(diary.createTime)
                                                        .from(diary)
                                                        .where(diary.user.id.eq(userId),
                                                      diary.createTime
                                                              .before(Time.getDateWithMinTime(year, month, day))
                                               )
                                                        .orderBy(diary.createTime.desc())
                                                        .fetchFirst();

        timeMap.put("before", beforeTime);

        final LocalDateTime AfterTime = jpaQueryFactory.select(diary.createTime).distinct()
                                              .from(diary)
                                              .where(diary.user.id.eq(userId),
                                                     diary.createTime
                                                             .after(Time.getDateWithMaxTime(year, month, day)))
                                              .orderBy(diary.createTime.asc())
                                              .fetchFirst();

        timeMap.put("after", AfterTime);

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

    /**
     * 식사일기의 식사시간 통계 정보를 조회합니다.
     * @param userId
     * @return DiaryStatisticsQueryDslReponseDTO
     */
    public DiaryStatisticsQueryDslResponseDTO selectDiaryStatistics(final int userId) {
        DiaryStatisticsQueryDslResponseDTO diaryStatisticsQueryDslReponseDTO = new DiaryStatisticsQueryDslResponseDTO();
        List<DiaryStatisticsQueryDslResponseDTO.DiarySubStatistics> diarySubStatisticsList = new ArrayList<>();
        diaryStatisticsQueryDslReponseDTO.setDiarySubStatisticsList(diarySubStatisticsList);
        AtomicLong totalSum = new AtomicLong();
        jpaQueryFactory.select(diary.diaryTime, diary.diaryTime.count())
                .from(diary)
                .where(diary.user.id.eq(userId))
                .groupBy(diary.diaryTime)
                .fetch()
                .stream()
                .forEach(tuple -> {
                    Long count = tuple.get(1, Long.class);
                    totalSum.getAndAdd(count);
                    diarySubStatisticsList.add(new DiaryStatisticsQueryDslResponseDTO.DiarySubStatistics(tuple.get(0, DiaryTime.class), count));
                });
        diaryStatisticsQueryDslReponseDTO.setTotalCount(totalSum.get());

        return diaryStatisticsQueryDslReponseDTO;
    }

    public Set<Integer> getEmptyMonthsInAYear(final int year, final int userId) {
        return jpaQueryFactory.select(diary.createTime.month(), diary.createTime.month().count())
                .from(diary)
                .where(diary.user.id.eq(userId).and(diary.createTime.year().eq(year)))
                .groupBy(diary.createTime.month())
                .fetch()
                .stream()
                .map(tuple -> tuple.get(0, Integer.class)).collect(Collectors.toSet());
    }
}
