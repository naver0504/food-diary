package com.fooddiary.api.repository.diary;

import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.diary.Time;
import com.fooddiary.api.entity.user.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fooddiary.api.entity.diary.QDiary.diary;
import static com.fooddiary.api.entity.diary.QImage.image;

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
                        diary.user.id.eq(userId),
                        diary.time.year.eq(year),
                        diary.time.month.eq(month),
                        diary.time.day.lt(startDay)
                )
                .orderBy(diary.time.day.desc())
                .limit(4)
                .fetch();


    }

    public Time getTime(final int imageId) {

        return jpaQueryFactory.select(diary.time)
                .from(diary)
                .leftJoin(diary.images, image)
                .where(image.id.eq(imageId))
                .fetchOne();

    }

    public Map<String, Time> getBeforeAndAfterTime(final int year, final int month, final int day, final int userId) {


        final Map<String, Time> times = new HashMap<>();
        final Time beforeTime = jpaQueryFactory.select(diary.time)
                .from(diary)
                .join(diary.images, image)
                .where(diary.user.id.eq(userId),
                        diary.time.createTime
                        .before(Time.getDateTime(year, month, day))
                        )
                .orderBy(diary.time.createTime.desc())
                .fetchFirst();

        if (beforeTime != null) {
            times.put("before", beforeTime);
        }

        //자기 자신을 포함한 후 + 이후의 시간을 가져온다.
        final List<Time> AfterTime = jpaQueryFactory.select(diary.time).distinct()
                .from(diary)
                .join(diary.images, image)
                .where(diary.user.id.eq(userId),
                        diary.time.createTime
                        .after(Time.getDateTime(year, month, day)))
                .orderBy(diary.time.createTime.asc())
                .limit(2)
                .fetch();


        if (AfterTime.size() == 2) {
            times.put("after", AfterTime.get(1));
        }

        return times;

    }

    public boolean existByUserId(final int userId) {
        Integer fetchFirst = jpaQueryFactory
                .selectOne()
                .from(diary)
                .join(diary.user, QUser.user)
                .fetchFirst();

        return fetchFirst != null;
    }
}
