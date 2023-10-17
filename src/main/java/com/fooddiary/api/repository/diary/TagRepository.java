package com.fooddiary.api.repository.diary;

import com.fooddiary.api.entity.diary.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Integer>{

    @Modifying
    @Query("delete from Tag t where t.id in :ids")
    void deleteAll(@Param("ids") List<Integer> ids);

    @Query("select t from Tag t where t.tagName = :tagName")
    Tag findTagByTagName(@Param("tagName") String tagName);
}
