package com.fooddiary.api.entity.tag;


import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.image.Image;
import com.fooddiary.api.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;
import java.util.stream.Collectors;

@Entity
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Getter
public class Tag {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, name = "tag_name")
    private String tagName;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id")
    private Diary diary;


    public void setDiary(Diary diary) {
        this.diary = diary;
    }

    public static List<String> toStringList(List<Tag> tags) {
        return tags.stream().map(Tag::getTagName).collect(Collectors.toList());
    }
}
