package com.fooddiary.api.entity.tag;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

import java.io.Serializable;

@Embeddable
@EqualsAndHashCode
@NoArgsConstructor
public class DiaryTagId implements Serializable {
    @Column(name = "diary_id")
    private Integer diaryId;
    @Column(name = "tag_id")
    private Integer tagId;

    public DiaryTagId(Integer diaryId, Integer tagId) {
        this.diaryId = diaryId;
        this.tagId = tagId;
    }
}
