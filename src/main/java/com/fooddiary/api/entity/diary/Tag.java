package com.fooddiary.api.entity.diary;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @Column(nullable = false, name = "tag_name", unique = true, length = 50)
    private String tagName;
    @OneToMany(mappedBy = "tag")
    private List<DiaryTag> diaryTags = new ArrayList<>();
    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;
}
