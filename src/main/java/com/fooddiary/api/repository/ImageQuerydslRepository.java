package com.fooddiary.api.repository;

import com.fooddiary.api.entity.diary.DiaryTime;
import com.fooddiary.api.entity.diary.Image;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import org.springframework.stereotype.Repository;

import java.util.List;

import static com.fooddiary.api.entity.diary.QImage.image;
import static com.fooddiary.api.entity.diary.QDiary.diary;
@Repository
public class ImageQuerydslRepository {

    private final JPAQueryFactory jpaQueryFactory;

    public ImageQuerydslRepository(EntityManager entityManager) {
        jpaQueryFactory = new JPAQueryFactory(entityManager);
    }

    public List<Image> findByDayImageId(final long dayImageId) {


        return jpaQueryFactory.selectFrom(image)
                .join(image.diary, diary)
                .where(
                        diary.id.eq(dayImageId)
                //        image.parentImage.isNull()
                )
                .orderBy(image.id.desc())
                .limit(5)
                .fetch();
    }

    public List<Image> findByYearAndMonthAndDay(final int year, final int month, final int day) {

        BooleanBuilder booleanBuilder = getBuilderWithTime(year, month, day);

        return jpaQueryFactory.selectFrom(
                        image)
                .leftJoin(image.diary, diary)
                // .leftJoin(image.diaryTags, tag)
                .fetchJoin()
                .where(booleanBuilder)
                .fetch();
    }

    public List<Image> findByYearAndMonthAndDayAndStartId(final int year, final int month, final int day, final int startId) {
        final BooleanBuilder booleanBuilder = getBuilderWithTime(year, month, day);
        booleanBuilder.and(image.id.lt(startId));
        return jpaQueryFactory.selectFrom(image)
                .leftJoin(image.diary, diary)
                .where(booleanBuilder)
                .orderBy(image.id.desc())
                .limit(5)
                .fetch();

    }

    private static BooleanBuilder getBuilderWithTime(int year, int month, int day) {
        BooleanBuilder booleanBuilder = new BooleanBuilder();
        // booleanBuilder.and(image.parentImage.isNull()); todo
        return booleanBuilder;
    }

    public void updateTimeStatus(final int imageId, final DiaryTime diaryTime) {
        /*
        jpaQueryFactory.update(image)
                .set(image.diaryTime, diaryTime)
              //  .where(image.id.eq(imageId).or(image.parentImage.id.eq(imageId))) todo
                .execute();

         */
    }
}

