package com.fooddiary.api.repository.diary;

import com.fooddiary.api.entity.diary.Diary;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;


@Repository
public interface DiaryRepository extends JpaRepository<Diary, Integer> {

    @Query("select d from Diary d join fetch d.images i where d.id = :id order by i.updateAt desc")
    Optional<Diary> findDiaryAndImagesById(@Param("id") int diaryId);

    @Query("select d from Diary d inner join fetch d.images i where d.createTime between :startDate and :endDate and d.user.id = :userId order by d.createTime asc, i.updateAt desc")
    List<Diary> findByYearAndMonth(@Param("startDate") LocalDateTime startDate, @Param("endDate") LocalDateTime endDate, @Param("userId") int userId);

    @Query("select d from Diary d inner join fetch d.images i " +
            "where d.createTime between :startDate and :endDate and d.user.id = :userId order by i.updateAt desc")
    List<Diary> findByYearAndMonthAndDay(@Param("startDate") LocalDateTime startDate,
                                             @Param("endDate") LocalDateTime endDate, @Param("userId") int userId);

    @Query("select count(d.id) from Diary d " +
            "where d.createTime between :startDate and :endDate and d.user.id = :userId")
    int getByYearAndMonthAndDayCount(@Param("startDate") LocalDateTime startDate,
                                     @Param("endDate") LocalDateTime endDate, @Param("userId") int userId);

    @Query("select count(d.id) from Diary d inner join d.images where d.id = :id")
    int getDiaryImagesCount(@Param("id") int id);

    @Query("select d from Diary d left join fetch d.images i where d.user.id = :userId and d.id > :id order by d.id asc")
    List<Diary> findByUserIdAndLimit(@Param("userId") int userId, @Param("id") long id, Pageable pageable);

    Diary findByUserIdAndId(@Param("userId") int userId, @Param("id") int id);
}
