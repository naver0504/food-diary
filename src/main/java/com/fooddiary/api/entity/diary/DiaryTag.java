package com.fooddiary.api.entity.diary;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class DiaryTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    @ManyToOne
    @JoinColumn(name = "diary_id")
    private Diary diary;
    @ManyToOne
    @JoinColumn(name = "tag_id")
    private Tag tag;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

}
