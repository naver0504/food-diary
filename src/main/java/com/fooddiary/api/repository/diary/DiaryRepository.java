package com.fooddiary.api.repository.diary;

import com.fooddiary.api.dto.response.search.DiarySearchSQLDTO;
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
public interface DiaryRepository extends JpaRepository<Diary, Long> {

    @Query("select d from Diary d join fetch d.images i where d.id = :id order by i.updateAt desc")
    Optional<Diary> findDiaryAndImagesById(@Param("id") long diaryId);

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
    int getDiaryImagesCount(@Param("id") long id);

    @Query("select d from Diary d left join fetch d.images i where d.user.id = :userId and d.id > :id order by d.id asc")
    List<Diary> findByUserIdAndLimit(@Param("userId") int userId, @Param("id") long id, Pageable pageable);

    Diary findByUserIdAndId(@Param("userId") int userId, @Param("id") long id);


    @Query(
            value = """
                    select diary.id, diary.place, diary.diary_time as diaryTime, x.thumbnail_file_name as thumbnailFileName from (
                    select diary_id, thumbnail_file_name, update_at, row_number() over (partition by diary_id order by update_at desc) as n
                    from image where (
                        diary_id in (select id from diary where user_id = :userId )
                     )
                    ) as x
                    inner join diary
                    on (diary.id = x.diary_id)
                    where n <= 1
                    order by diary.id asc, x.update_at desc
                    """,
            nativeQuery = true
    )
    List<DiarySearchSQLDTO.DiarySearchNoTagSQLDTO> getSearchResultWithLatestImageAndNoTag(@Param("userId") int userId);

    @Query(
            value = """
                    select diary.id, diary.place, x.thumbnail_file_name as thumbnailFileName,  tag.tag_name as tagName  from (
                    select diary_id, thumbnail_file_name, update_at, row_number() over (partition by diary_id order by update_at desc) as n
                    from image where (
                        diary_id in (select id from diary where user_id = :userId )
                     )
                    ) as x
                    inner join diary
                    on (diary.id = x.diary_id)
                    inner join diary_tag
                    on (diary.id = diary_tag.diary_id)
                    inner join tag
                    on (diary_tag.tag_id = tag.id)
                    where n <= 1
                    order by diary.id asc, x.update_at desc
                    """,
            nativeQuery = true
    )
    List<DiarySearchSQLDTO.DiarySearchWithTagSQLDTO> getSearchResultWithLatestImageAndTag(@Param("userId") int userId);

}
