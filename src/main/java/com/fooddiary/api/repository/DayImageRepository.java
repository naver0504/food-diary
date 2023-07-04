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
    public List<DayImage> findByYearAndMonth(@Param("year") int year, @Param("month") int month, @Param("userId") int userId);

    @Query("select d from DayImage d " +
            "where d.time.year = :year and d.time.month = :month and d.time.day = :day and d.user.id = :userId")
    public DayImage findByYearAndMonthAndDay(@Param("year") int year,
                                             @Param("month") int month, @Param("day") int day, @Param("userId") int userId);



}
