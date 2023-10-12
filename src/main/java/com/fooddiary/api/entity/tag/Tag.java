package com.fooddiary.api.entity.tag;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class Tag {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false, name = "tag_name", unique = true)
    private String tagName;
    @OneToMany(mappedBy = "tag")
    private List<DiaryTag> diaryTags = new ArrayList<>();
}
