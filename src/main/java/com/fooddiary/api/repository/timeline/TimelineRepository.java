package com.fooddiary.api.repository.timeline;

import com.fooddiary.api.entity.diary.Diary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface TimelineRepository extends JpaRepository<Diary, Integer> {
}
