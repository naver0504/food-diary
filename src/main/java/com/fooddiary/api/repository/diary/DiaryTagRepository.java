package com.fooddiary.api.repository.diary;

import com.fooddiary.api.entity.diary.DiaryTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiaryTagRepository extends JpaRepository<DiaryTag, Integer>{
    @Modifying(clearAutomatically = true)
    @Query("delete from DiaryTag t where t.id in :ids")
    void deleteAllWithIds(@Param("ids") List<Long> ids);
}
