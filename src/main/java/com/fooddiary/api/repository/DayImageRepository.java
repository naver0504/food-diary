package com.fooddiary.api.repository;

import com.fooddiary.api.entity.image.DayImage;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;


@Repository
public interface DayImageRepository extends JpaRepository<DayImage, Integer> {

    @Query("select d from DayImage d where d.time.year = :year and d.time.month = :month and d.user.id = :userId")
    List<DayImage> findByYearAndMonth(@Param("year") int year, @Param("month") int month, @Param("userId") int userId);

    @Query("select d from DayImage d join fetch d.images " +
            "where d.time.year = :year and d.time.month = :month and d.time.day = :day and d.user.id = :userId")
    DayImage findByYearAndMonthAndDay(@Param("year") int year,
                                             @Param("month") int month, @Param("day") int day, @Param("userId") int userId);

    @Query("select count(*) from DayImage d where d.user.id = :userId")
    int getDayImageCount(@Param("userId") int userId);


    @Query("select d from DayImage d join fetch d.images  " +
            "where d.time.year = :year and d.time.month = :month and d.user.id = :userId " +
            "order by d.time.day desc ")
    List<DayImage> findDayImageByYearAndMonthOrderByDayDesc(@Param("year") int year, @Param("month") int month, @Param("userId") int userId);


}
