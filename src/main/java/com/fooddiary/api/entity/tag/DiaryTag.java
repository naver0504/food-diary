package com.fooddiary.api.entity.tag;


import com.fooddiary.api.entity.diary.Diary;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
public class DiaryTag {

    @EmbeddedId
    private DiaryTagId id;
    @MapsId("diaryId")
    @JoinColumn(name = "diary_id")
    private Diary diary;
    @MapsId("tagId")
    @JoinColumn(name = "tag_id")
    private Tag tag;

}
