package com.fooddiary.api.repository.diary;

import com.fooddiary.api.entity.diary.Diary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DiaryRepository extends JpaRepository<Diary, Integer> {

    @Query("select d from Diary d join d.images i where i.id = :id")
    Optional<Diary> findDiaryAndImagesById(@Param("id") int diaryId);


    @Query("select d from Diary d inner join d.images where d.time.year = :year and d.time.month = :month and d.user.id = :userId order by d.time.day asc")
    List<Diary> findByYearAndMonth(@Param("year") int year, @Param("month") int month, @Param("userId") int userId);

    @Query("select d from Diary d inner join d.images " +
            "where d.time.year = :year and d.time.month = :month and d.time.day = :day and d.user.id = :userId")
    Diary findByYearAndMonthAndDay(@Param("year") int year,
                                             @Param("month") int month, @Param("day") int day, @Param("userId") int userId);

    @Query("select count(d.id) from Diary d " +
            "where d.time.year = :year and d.time.month = :month and d.time.day = :day and d.user.id = :userId")
    int getByYearAndMonthAndDayCount(@Param("year") int year,
                                   @Param("month") int month, @Param("day") int day, @Param("userId") int userId);

    @Query("select count(d.id) from Diary d inner join d.images where d.id = :id")
    int getDiaryImagesCount(@Param("id") int id);


    @Query("select d from Diary d left join fetch d.images i where d.user.id = :userId and d.id > :id order by d.id asc")
    List<Diary> findByUserIdAndLimit(@Param("userId") int userId, @Param("id") int id, Pageable pageable);
}
