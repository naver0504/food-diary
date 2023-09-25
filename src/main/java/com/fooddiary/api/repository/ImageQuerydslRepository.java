package com.fooddiary.api.repository;

import com.fooddiary.api.entity.image.Image;
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

    public List<Image> findByYearAndMonthAndDay(final int year, final int month, final int day, final int userId) {

        BooleanBuilder booleanBuilder = getImageWithTime(year, month, day, userId);

        return jpaQueryFactory.selectFrom(
                        image)
                .leftJoin(image.dayImage, dayImage)
                .leftJoin(image.tags, tag).fetchJoin()
                .where(booleanBuilder)
                .fetch();
    }

    private static BooleanBuilder getImageWithTime(int year, int month, int day, int userId) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        booleanBuilder.and(dayImage.time.year.eq(year));
        booleanBuilder.and(dayImage.time.month.eq(month));
        booleanBuilder.and(dayImage.time.day.eq(day));
        booleanBuilder.and(image.user.id.eq(userId));
        booleanBuilder.and(image.parentImage.isNull());
        return booleanBuilder;
    }
}

