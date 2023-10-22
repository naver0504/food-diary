package com.fooddiary.api.repository.timeline;

import com.fooddiary.api.dto.response.timeline.TimelineDiaryImageSQLDTO;
import com.fooddiary.api.entity.diary.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TimelineRepository extends JpaRepository<Diary, Integer> {

    @Query(value = "select\n" +
            "        d1_0.id,\n" +
            "        d1_0.create_time,\n" +
            "        i1_0.stored_file_name, \n" +
            "        i1_0.id as image_id\n" +
            "    from\n" +
            "        diary d1_0 \n" +
            "    join\n" +
            "        (select * from image order by image.update_at desc limit 1) i1_0 \n" +
            "    on (i1_0.diary_id = d1_0.id) \n" +
            "    where\n" +
            "        d1_0.user_id = :userId \n" +
            "        and d1_0.create_time between :startDate and :endDate\n" +
            "    order by\n" +
            "        d1_0.create_time asc,\n" +
            "        i1_0.update_at desc ", nativeQuery = true)
    List<TimelineDiaryImageSQLDTO> getTimeLineDiaryWithLatestImage(@Param("userId") int userId,
                                                                   @Param("startDate") LocalDateTime startDate,
                                                                   @Param("endDate") LocalDateTime endDate);
}
