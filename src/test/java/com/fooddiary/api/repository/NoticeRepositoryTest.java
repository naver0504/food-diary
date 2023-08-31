package com.fooddiary.api.repository;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase.Replace;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.entity.notice.Notice;

@DataJpaTest
@AutoConfigureTestDatabase(replace = Replace.NONE)
@ActiveProfiles(Profiles.TEST)
class NoticeRepositoryTest {
    @Autowired
    NoticeRepository noticeRepository;

    @Test
    @Transactional
    void selectNoticeListByIdPaging() {
        final List<Notice> saveList = new ArrayList<>();
        Notice notice = new Notice();
        notice.setTitle("신규 출시");
        notice.setContent("2023년도부터 개발하여 2024년도에 드디에 앱을 출시했습니다. 많은 이용 부탁드립니다.");
        notice.setAvailable(true);
        saveList.add(notice);

        notice = new Notice();
        notice.setTitle("긴급패치");
        notice.setContent("로그인 후 사진조회가 안되는 문제 수정");
        saveList.add(notice);
        noticeRepository.saveAll(saveList);

        final List<Notice> noticeList = noticeRepository.selectgetNoticeListByIdPaging(1, true, PageRequest.of(0, 2));

        Assertions.assertEquals(noticeList.size(), 1);
    }
}
