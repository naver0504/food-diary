package com.fooddiary.api.repository;

import com.fooddiary.api.entity.tag.DiaryTag;
import com.fooddiary.api.entity.tag.Tag;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface TagRepository extends JpaRepository<Tag, Integer>{

    @Modifying
    @Query("delete from Tag t where t.id in :ids")
    void deleteAll(@Param("ids") List<Integer> ids);

    @Query("select t from Tag t where t.tagName =: tagName")
    DiaryTag findTagByTagName(@Param("tagName") String tagName);
}
