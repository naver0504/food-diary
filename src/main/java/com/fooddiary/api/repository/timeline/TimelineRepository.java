package com.fooddiary.api.repository.timeline;

import com.fooddiary.api.dto.response.timeline.TimelineDiaryImageSQLDTO;
import com.fooddiary.api.entity.diary.Diary;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimelineRepository extends JpaRepository<Diary, Integer> {

    // https://stackoverflow.com/questions/2129693/using-limit-within-group-by-to-get-n-results-per-group
    @Query(// + "x.diaryId, " 쿼리 디버깅용
// + ", x.update_at as imageUpdateAt \n" 쿼리 디버깅용
            value = """
                    select diary.id, diary.create_time as createTime,\s
                    x.stored_file_name as storedFileName, x.id as imageId from (
                     select diary_id, stored_file_name, id, update_at, row_number() over (partition by diary_id order by update_at desc) as n\s
                     from image where (
                     diary_id in (select id from diary where user_id = :userId and create_time between :startDate and :endDate)
                     )
                    ) as x
                    inner join diary
                    on (diary.id = x.diary_id)
                    where n <= 1
                    order by diary.id asc, x.update_at desc\s""", nativeQuery = true)
    List<TimelineDiaryImageSQLDTO> getTimeLineDiaryWithLatestImage(@Param("userId") int userId,
                                                                   @Param("startDate") LocalDateTime startDate,
                                                                   @Param("endDate") LocalDateTime endDate, Pageable page);
}
