package com.fooddiary.api.repository;

import com.fooddiary.api.entity.image.DayImage;
import com.fooddiary.api.entity.image.Time;
import com.fooddiary.api.entity.user.QUser;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.fooddiary.api.entity.image.QDayImage.*;
import static com.fooddiary.api.entity.image.QImage.*;

@Repository
@Slf4j
public class DayImageQuerydslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public DayImageQuerydslRepository(EntityManager entityManager) {
        jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    public List<DayImage> getTimeLineDayImage(final int year, final int month, final int startDay, final int userId) {
        return jpaQueryFactory.selectFrom(dayImage)
                .where(
                        dayImage.user.id.eq(userId),
                        dayImage.time.year.eq(year),
                        dayImage.time.month.eq(month),
                        dayImage.time.day.lt(startDay)
                )
                .orderBy(dayImage.time.day.desc())
                .limit(4)
                .fetch();


    }

    public Time getTime(final int imageId) {

        return jpaQueryFactory.select(dayImage.time)
                .from(dayImage)
                .leftJoin(dayImage.images, image)
                .where(image.id.eq(imageId))
                .fetchOne();

    }

    public Map<String, Time> getBeforeAndAfterTime(final int year, final int month, final int day, final int userId) {


        final Map<String, Time> times = new HashMap();
        final Time beforeTime = jpaQueryFactory.select(dayImage.time)
                .from(dayImage)
                .join(dayImage.images, image)
                .where(dayImage.user.id.eq(userId),
                        dayImage.time.localDateTime
                        .before(Time.getDateTime(year, month, day))
                        )
                .orderBy(dayImage.time.localDateTime.desc())
                .fetchFirst();
        System.out.println("beforeTime = " + beforeTime);

        if (beforeTime != null) {
            times.put("before", beforeTime);
        }

        //자기 자신을 포함한 후 + 이후의 시간을 가져온다.
        final List<Time> AfterTime = jpaQueryFactory.select(dayImage.time).distinct()
                .from(dayImage)
                .join(dayImage.images, image)
                .where(dayImage.user.id.eq(userId),
                        dayImage.time.localDateTime
                        .after(Time.getDateTime(year, month, day)))
                .orderBy(dayImage.time.localDateTime.asc())
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
                .from(dayImage)
                .join(dayImage.user, QUser.user)
                .fetchFirst();

        return fetchFirst != null;
    }
}
