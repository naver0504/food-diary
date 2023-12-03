package com.fooddiary.api.repository.diary;

import com.fooddiary.api.dto.response.diary.DiaryStatisticsQueryDslResponseDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.diary.DiaryTime;
import com.fooddiary.api.entity.diary.Time;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;

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
}
