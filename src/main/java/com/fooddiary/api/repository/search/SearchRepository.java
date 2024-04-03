package com.fooddiary.api.repository.search;

import com.fooddiary.api.dto.response.search.ConditionSearchSQLDTO;
import com.fooddiary.api.dto.response.search.DiarySearchSQLDTO;
import com.fooddiary.api.dto.response.search.SearchSQLDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.diary.DiaryTime;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface SearchRepository extends JpaRepository<Diary, Integer> {


    @Query(
            value = """
            select u.category as categoryName, categoryType from (
            (select d1.diary_time as category, count(diary_time) as c, 'DIARY_TIME' as categoryType from diary as d1 where d1.user_id = :userId group by d1.diary_time ) 
            union all
            (select d2.place as category, count(d2.place)  as c, 'PLACE' as categoryType from diary as d2 where d2.user_id = :userId and d2.place is not null group by d2.place )
            ) as u order by u.c desc
            """, nativeQuery = true)
    List<SearchSQLDTO> getSearchResultWithoutConditionAndTag(@Param("userId") int id, Pageable pageable);

    @Query(
            value = """
            select u.category as categoryName, categoryType from 
            (
                (
                select t.tag_name as category, count(t.tag_name) as c, 'TAG' as categoryType from diary as d1 
                inner join diary_tag as dt on d1.id = dt.diary_id 
                inner join tag as t on t.id = dt.tag_id 
                where d1.user_id = :userId group by t.tag_name
                ) 
            union all
                (  
                select d2.place as category, count(d2.place) as c,'PLACE' as categoryType from diary as d2 
                where d2.user_id = :userId and d2.place is not null group by d2.place 
                )
            ) as u order by u.c desc
            """, nativeQuery = true)
    List<SearchSQLDTO> getSearchResultWithoutCondition(@Param("userId") int id, Pageable pageable);

    @Query(
            value = """
            select  d.id, x.thumbnail_file_name as thumbnailFileName from diary as d 
            inner join 
            ( 
                select diary_id, thumbnail_file_name, update_at,
                row_number() over (partition by diary_id order by update_at desc) as n 
                from image 
            ) as x 
            on d.id = x.diary_id
            where d.user_id = :userId and d.diary_time = :#{#diaryTime.name()} and n <= 1
            order by d.id desc, x.update_at desc
            """, nativeQuery = true)
    List<DiarySearchSQLDTO> getSearchResultWithDiaryTime(@Param("userId") int id, @Param("diaryTime") DiaryTime diaryTime, Pageable pageable);


    @Query(
            value = """
            select  d.id, x.thumbnail_file_name as thumbnailFileName from diary as d 
            inner join 
            ( 
                select diary_id, thumbnail_file_name, update_at,
                row_number() over (partition by diary_id order by update_at desc) as n 
                from image 
            ) as x 
            on d.id = x.diary_id
            where d.user_id = :userId and d.place = :place and n <= 1
            order by d.id desc, x.update_at desc
            """, nativeQuery = true)
    List<DiarySearchSQLDTO> getSearchResultWithPlace(@Param("userId") int id, @Param("place") String place, Pageable pageable);

    @Query(
            value = """
            select  d.id, x.thumbnail_file_name as thumbnailFileName from diary as d 
            inner join 
            ( 
                select diary_id, thumbnail_file_name, update_at,
                row_number() over (partition by diary_id order by update_at desc) as n 
                from image 
            ) as x 
            on d.id = x.diary_id
            where d.user_id = :userId and binary(d.place) = :place and n <= 1
            order by d.id desc, x.update_at desc
            """, nativeQuery = true)
    List<DiarySearchSQLDTO> getSearchResultWithPlaceNoLimit(@Param("userId") int id, @Param("place") String place);


    @Query(
            value = """
            select d.id, x.thumbnail_file_name as thumbnailFileName from diary as d 
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
    List<DiarySearchSQLDTO> getSearchResultWithTag(@Param("userId") int id, @Param("tagName") String tagName, Pageable pageable);

    @Query(
            value = """
            select d.id, x.thumbnail_file_name as thumbnailFileName from diary as d 
            inner join 
            ( 
                select diary_id, thumbnail_file_name, update_at,  
                row_number() over (partition by diary_id order by update_at desc) as n 
                from image 
            ) as x 
            on d.id = x.diary_id
            inner join diary_tag as dt on (dt.diary_id = d.id)
            inner join tag as t on (t.id = dt.tag_id)
            where d.user_id = :userId and binary(t.tag_name) = :tagName and n <= 1
            order by d.id desc, x.update_at desc""", nativeQuery = true)
    List<DiarySearchSQLDTO> getSearchResultWithTagNoLimit(@Param("userId") int id, @Param("tagName") String tagName);

    @Query(
            value = """
            select d.id, x.thumbnail_file_name as thumbnailFileName from diary as d 
            inner join 
            ( 
                select diary_id, thumbnail_file_name, update_at,
                row_number() over (partition by diary_id order by update_at desc) as n 
                from image 
            ) as x 
            on d.id = x.diary_id
            where d.user_id = :userId and d.diary_time = :#{#diaryTime.name()} and n <= 1
            order by d.id desc, x.update_at desc""", nativeQuery = true)
    List<DiarySearchSQLDTO> getStatisticsSearchResultWithDiaryTimeNoLimit(@Param("userId") int userId, @Param("diaryTime") DiaryTime diaryTime);

    @Query(
            value = """
            select u.categoryName, u.categoryType from
            	(
            		(
            		    select binary(t.tag_name) as categoryName, count(t.tag_name) as c, 'TAG' as categoryType, t.tag_name as org from diary as d1
                        inner join diary_tag as dt on d1.id = dt.diary_id inner join tag as t on t.id = dt.tag_id 
                        where d1.user_id = :userId and t.tag_name like :condition group by binary(t.tag_name), t.id 
                    )
                    union all
                    (
                        select binary(d2.place) as categoryName, count(d2.place) as c, 'PLACE' as categoryType, d2.place as org from diary as d2
                        where d2.place like :condition and d2.user_id = :userId group by binary(d2.place), d2.id 
                    )
                    union all
                    (
                        select binary(d1.diary_time) as categoryName, count(d1.diary_time) as c, 'DIARY_TIME' as categoryType, d1.diary_time as org  from diary as d1 
                        where d1.user_id = :userId and  d1.diary_time in :diaryTimeList group by binary(d1.diary_time), d1.id 
                    )
            	) as u 
            order by u.c desc, u.categoryName desc, org""", nativeQuery = true)
    List<ConditionSearchSQLDTO> getSearchResultWithLowerCondition(@Param("userId") int userId, @Param("condition") String condition, @Param("diaryTimeList") List<String> diaryTimeList);

    @Query(
            value = """
            select u.categoryName, u.categoryType from
            	(
            		(
            		    select binary(t.tag_name) as categoryName, count(t.tag_name) as c, 'TAG' as categoryType, t.tag_name as org from diary as d1
                        inner join diary_tag as dt on d1.id = dt.diary_id inner join tag as t on t.id = dt.tag_id 
                        where d1.user_id = :userId and t.tag_name like :condition group by binary(t.tag_name), t.id 
                    )
                    union all
                    (
                        select binary(d2.place) as categoryName, count(d2.place) as c, 'PLACE' as categoryType, d2.place as org from diary as d2
                        where d2.place like :condition and d2.user_id = :userId group by binary(d2.place), d2.id 
                    )
                    union all
                    (
                        select binary(d1.diary_time) as categoryName, count(d1.diary_time) as c, 'DIARY_TIME' as categoryType, d1.diary_time as org  from diary as d1 
                        where d1.user_id = :userId and  d1.diary_time in :diaryTimeList group by binary(d1.diary_time), d1.id 
                    )
            	) as u 
            order by u.c desc, u.categoryName asc, org """, nativeQuery = true)
    List<ConditionSearchSQLDTO> getSearchResultWithUpperCondition(@Param("userId") int userId, @Param("condition") String condition, @Param("diaryTimeList") List<String> diaryTimeList);


}

