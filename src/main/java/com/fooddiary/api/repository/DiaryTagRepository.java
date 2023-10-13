package com.fooddiary.api.repository;

import com.fooddiary.api.entity.tag.DiaryTag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface DiaryTagRepository extends JpaRepository<DiaryTag, Integer>{
/*
    @Modifying
    @Query("delete from DiaryTag t where t.id in :ids")
    void deleteAll(@Param("ids") List<Integer> ids);

 */
}
