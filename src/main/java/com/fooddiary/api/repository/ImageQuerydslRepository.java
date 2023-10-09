package com.fooddiary.api.repository;

import com.fooddiary.api.entity.image.Image;
import com.fooddiary.api.entity.image.TimeStatus;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.fooddiary.api.entity.image.QDayImage.dayImage;
import static com.fooddiary.api.entity.image.QImage.image;
import static com.fooddiary.api.entity.tag.QTag.tag;

@Repository
public class ImageQuerydslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public ImageQuerydslRepository(EntityManager entityManager) {
        jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    public List<Image> findByDayImageId(final int dayImageId, final int userId) {


        return jpaQueryFactory.selectFrom(image)
                .join(image.dayImage, dayImage)
                .where(
                        image.user.id.eq(userId),
                        dayImage.id.eq(dayImageId),
                        image.parentImage.isNull()
                )
                .orderBy(image.id.desc())
                .limit(5)
                .fetch();
    }

    public List<Image> findByYearAndMonthAndDay(final int year, final int month, final int day, final int userId) {

        BooleanBuilder booleanBuilder = getBuilderWithTime(year, month, day, userId);

        return jpaQueryFactory.selectFrom(
                        image)
                .leftJoin(image.dayImage, dayImage)
                .leftJoin(image.tags, tag).fetchJoin()
                .where(booleanBuilder)
                .fetch();
    }

    public List<Image> findByYearAndMonthAndDayAndStartId(final int year, final int month, final int day, final int startId, final int userId) {
        BooleanBuilder booleanBuilder = getBuilderWithTime(year, month, day, userId);
        booleanBuilder.and(image.id.lt(startId));
        return jpaQueryFactory.selectFrom(image)
                .leftJoin(image.dayImage, dayImage)
                .where(booleanBuilder)
                .orderBy(image.id.desc())
                .limit(5)
                .fetch();

    }

    private static BooleanBuilder getBuilderWithTime(int year, int month, int day, int userId) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(image.user.id.eq(userId));
        booleanBuilder.and(dayImage.time.year.eq(year));
        booleanBuilder.and(dayImage.time.month.eq(month));
        booleanBuilder.and(dayImage.time.day.eq(day));
        booleanBuilder.and(image.parentImage.isNull());
        return booleanBuilder;
    }

    public void updateTimeStatus(final int imageId, final TimeStatus timeStatus) {
        jpaQueryFactory.update(image)
                .set(image.timeStatus, timeStatus)
                .where(image.id.eq(imageId).or(image.parentImage.id.eq(imageId)))
                .execute();
    }
}

