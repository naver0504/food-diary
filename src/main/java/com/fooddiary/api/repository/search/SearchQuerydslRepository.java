package com.fooddiary.api.repository.search;

import com.fooddiary.api.entity.user.User;
import com.querydsl.core.Tuple;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;


import java.util.List;

import static com.fooddiary.api.entity.diary.QDiary.*;
import static com.fooddiary.api.entity.diary.QDiaryTag.diaryTag;
import static com.fooddiary.api.entity.diary.QTag.*;

@Repository
@Slf4j
public class SearchQuerydslRepository {

    private final JPAQueryFactory queryFactory;

    public SearchQuerydslRepository(EntityManager entityManager) {
        this.queryFactory = new JPAQueryFactory(entityManager);
    }

    public boolean existByPlace(final User user, final String place) {
        Integer result = queryFactory.selectOne()
                .from(diary)
                .where(
                        diary.user.id.eq(user.getId()),
                        diary.place.isNotNull(),
                        diary.place.eq(place)
                )
                .fetchFirst();

        return result != null;
    }

    public List<String> getTagNameListContainSearchCond(final User user, final String searchCond) {
        return queryFactory.select(tag.tagName)
                .from(diaryTag)
                .innerJoin(diaryTag.diary, diary)
                .innerJoin(diaryTag.tag, tag)
                .where(
                        diary.user.id.eq(user.getId()),
                        tag.tagName.contains(searchCond)
                )
                .groupBy(tag.tagName)
                .orderBy(tag.tagName.count().desc())
                .fetch();
    }

}
