package com.fooddiary.api.repository.search;

import com.fooddiary.api.dto.response.search.DiarySearchSQLDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.diary.DiaryTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchRepository extends JpaRepository<Diary, Integer> {

    @Query(
            value = """
            select d.id, d.diary_time as diaryTime, x.thumbnail_file_name as thumbnailFileName from Diary as d 
            inner join 
            ( 
                select diary_id, thumbnail_file_name, update_at,
                row_number() over (partition by diary_id order by update_at desc) as n 
                from image 
            ) as x 
            on d.id = x.diary_id
            where d.user_id = :userId and d.diary_time = :#{#diaryTime.name()} and n <= 1
            order by d.id desc, x.update_at desc""", nativeQuery = true)
    List<DiarySearchSQLDTO.DiarySearchWithDiaryTimeSQLDTO> getSearchResultWithDiaryTimeNoLimit(@Param("userId") int userId,
                                                                                               @Param("diaryTime") DiaryTime diaryTime);

    @Query(
            value = """
            select d.id, d.place, x.thumbnail_file_name as thumbnailFileName from Diary as d 
            inner join 
            ( 
                select diary_id, thumbnail_file_name, update_at,  
                row_number() over (partition by diary_id order by update_at desc) as n 
                from image
            ) as x
            on d.id = x.diary_id
            where d.user_id = :userId and d.place = :place and n <= 1
            order by d.id desc, x.update_at desc""", nativeQuery = true)
    List<DiarySearchSQLDTO.DiarySearchWithPlaceSQLDTO> getSearchResultWithPlaceNoLimit(@Param("userId") int id,
                                                                                       @Param("place") String place);
    @Query(
            value = """
            select d.id, t.tag_name as tagName, x.thumbnail_file_name as thumbnailFileName from Diary as d 
            inner join 
            ( 
                select diary_id, thumbnail_file_name, update_at,  
                row_number() over (partition by diary_id order by update_at desc) as n 
                from image 
            ) as x 
            on d.id = x.diary_id
            inner join diary_tag as dt on (dt.diary_id = d.id)
            inner join tag as t on (t.id = dt.tag_id)
            where d.user_id = :userId and t.tag_name = :tagName and n <= 1
            order by d.id desc, x.update_at desc""", nativeQuery = true)
    List<DiarySearchSQLDTO.DiarySearchWithTagSQLDTO> getSearchResultWithTagNoLimit(@Param("userId") int id,
                                                                                   @Param("tagName") String tagName);

}

