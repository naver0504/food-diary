package com.fooddiary.api.repository;

import com.fooddiary.api.entity.image.DayImage;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface DayImageRepository extends JpaRepository<DayImage, Integer> {

    /*
    @Query("select d from DayImage d join  d.images i where i.id = :imageId and d.user.id = :userId")
    Optional<DayImage> findByImageIdAndUserId(@Param("imageId") int imageId, @Param("userId") int userId);


    @Query("select d from DayImage d where d.time.year = :year and d.time.month = :month and d.user.id = :userId order by d.time.day asc")
    List<DayImage> findByYearAndMonth(@Param("year") int year, @Param("month") int month, @Param("userId") int userId);

    @Query("select d from DayImage d join fetch d.images " +
            "where d.time.year = :year and d.time.month = :month and d.time.day = :day and d.user.id = :userId ")
    DayImage findByYearAndMonthAndDay(@Param("year") int year,
                                             @Param("month") int month, @Param("day") int day, @Param("userId") int userId);

    @Query("select count(*) from DayImage d where d.user.id = :userId")
    int getDayImageCount(@Param("userId") int userId);


    @Query("select d from DayImage d left join fetch d.images i where d.user.id = :userId and d.id > :id order by d.id asc")
    List<DayImage> findByUserIdAndLimit(@Param("userId") int userId, @Param("id") int id, Pageable pageable);

     */
}
