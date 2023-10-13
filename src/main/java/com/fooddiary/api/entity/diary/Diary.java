package com.fooddiary.api.entity.diary;


import com.fooddiary.api.entity.image.DiaryTime;
import com.fooddiary.api.entity.image.Image;
import com.fooddiary.api.entity.image.Time;
import com.fooddiary.api.entity.tag.DiaryTag;
import com.fooddiary.api.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Embedded
    private Time time;

    @Enumerated(EnumType.STRING)
    private DiaryTime diaryTime;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Image> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String memo;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL)
    @Builder.Default
    private List<DiaryTag> diaryTags = new ArrayList<>();

    public static Diary createDiaryImage(final LocalDateTime dateTime, final User user, final DiaryTime diaryTime) {
        return Diary.builder()
                .time(new Time(dateTime))
                .createAt(LocalDateTime.now())
                .diaryTime(diaryTime)
                .user(user)
                .build();
    }
}

