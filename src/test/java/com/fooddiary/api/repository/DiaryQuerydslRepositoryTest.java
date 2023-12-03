package com.fooddiary.api.repository;

import com.fooddiary.api.common.constants.Profiles;
import com.fooddiary.api.dto.response.diary.DiaryStatisticsQueryDslResponseDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.diary.DiaryTime;
import com.fooddiary.api.entity.user.User;
import com.fooddiary.api.repository.diary.DiaryQuerydslRepository;
import com.fooddiary.api.repository.diary.DiaryRepository;
import com.fooddiary.api.repository.user.UserRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@DataJpaTest
@Import({DiaryQuerydslRepository.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles(Profiles.TEST)
public class DiaryQuerydslRepositoryTest {

    @Autowired
    DiaryQuerydslRepository diaryQuerydslRepository;
    @Autowired
    UserRepository userRepository;
    @Autowired
    DiaryRepository diaryRepository;

    /**
     * 식사일기의 식사시간 통계 정보를 조회결과를 검증합니다.
     */
    @Test
    @Transactional
    void selectDiaryStatistics() {
        User user = new User();
        user.setCreateAt(LocalDateTime.now());
        user.setEmail("jasuil@daum.net");
        userRepository.save(user);

        Diary diary = new Diary();
        diary.setCreateAt(LocalDateTime.now());
        diary.setCreateTime(LocalDateTime.now());
        diary.setDiaryTime(DiaryTime.BREAKFAST);
        diary.setUser(user);
        diaryRepository.save(diary);

        DiaryStatisticsQueryDslResponseDTO result = diaryQuerydslRepository.selectDiaryStatistics(user.getId());
        List<DiaryStatisticsQueryDslResponseDTO.DiarySubStatistics> diarySubStatistics = result.getDiarySubStatisticsList();
        Assertions.assertEquals(diarySubStatistics.size(), 1);
        Assertions.assertEquals(diarySubStatistics.get(0).getDiaryTime(), DiaryTime.BREAKFAST);
        Assertions.assertEquals(diarySubStatistics.get(0).getCount(), 1);
    }
}
