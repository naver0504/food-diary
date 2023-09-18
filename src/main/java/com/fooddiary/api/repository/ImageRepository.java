package com.fooddiary.api.repository;

import com.fooddiary.api.entity.image.Image;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ImageRepository extends JpaRepository<Image, Integer> {

    @Query("select i from Image i join DayImage d " +
            "where d.time.year = :year and d.time.month = :month and d.time.day = :day and d.user.id = :userId and i.parentImage is null")
    List<Image> findByYearAndMonthAndDay(@Param("year") int year,
                                         @Param("month") int month, @Param("day") int day, @Param("userId") int userId);

}
