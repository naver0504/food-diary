package com.fooddiary.api.repository.search;

import com.fooddiary.api.entity.user.User;
import com.querydsl.jpa.impl.JPAQueryFactory;
import jakarta.persistence.EntityManager;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;



import static com.fooddiary.api.entity.diary.QDiary.*;

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

}
