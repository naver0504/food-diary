package com.fooddiary.api;

import org.jooq.DSLContext;
import org.jooq.Result;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit.jupiter.SpringExtension;

import com.fooddiary.api.jooq.Tables;
import com.fooddiary.api.jooq.tables.records.UserRecord;

@SpringBootTest
@ActiveProfiles("test")
@ContextConfiguration(classes = ApiApplication.class)
class ApiApplicationTests {

	@Autowired
	DSLContext dslContext;
	@Test
	void query_test() {
		Result<UserRecord> r = dslContext.selectFrom(Tables.USER).fetch();
		System.out.println(r);
	}

}
