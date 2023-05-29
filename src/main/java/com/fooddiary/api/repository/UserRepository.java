package com.fooddiary.api.repository;

import com.fooddiary.api.dao.UserDao;
import lombok.RequiredArgsConstructor;
import org.jooq.DSLContext;
import org.springframework.stereotype.Repository;

import static com.fooddiary.api.jooq.Tables.SESSION;

@Repository
@RequiredArgsConstructor
public class UserRepository {

    private final DSLContext dslContext;

    public void insertSession(final UserDao userDao) {
        dslContext.insertInto(SESSION)
                .set(SESSION.USER_EMAIL, userDao.getUserEmail())
                .set(SESSION.TOKEN, userDao.getToken()).execute();
    }
}
