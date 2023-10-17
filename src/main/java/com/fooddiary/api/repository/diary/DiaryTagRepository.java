package com.fooddiary.api.repository.diary;

import com.fooddiary.api.entity.diary.DiaryTag;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DiaryTagRepository extends JpaRepository<DiaryTag, Integer>{
/*
    @Modifying
    @Query("delete from DiaryTag t where t.id in :ids")
    void deleteAll(@Param("ids") List<Integer> ids);

 */
}
