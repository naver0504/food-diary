package com.fooddiary.api.entity.diary;


import com.fooddiary.api.dto.request.diary.PlaceInfoDTO;
import com.fooddiary.api.entity.image.DiaryTime;
import com.fooddiary.api.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;
import org.springframework.util.StringUtils;

@Entity
@Getter
@Setter
@AllArgsConstructor
@NoArgsConstructor
public class Diary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private LocalDateTime createTime; // 사용자가 선택한 식사일기 시간
    //@Embedded
    //private Time time;

    @Enumerated(EnumType.STRING)
    private DiaryTime diaryTime;

    private String place;

    @Column(columnDefinition = "GEOMETRY")
    private Point geography;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;
    private LocalDateTime updateAt;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<Image> images = new ArrayList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    private String memo;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiaryTag> diaryTags = new ArrayList<>();

    public static Diary createDiary(final LocalDateTime dateTime, final User user, final DiaryTime diaryTime, final PlaceInfoDTO placeInfo) {
        Diary diary = new Diary();
        diary.setCreateTime(setCreateTime(dateTime, diaryTime));
        diary.setCreateAt(LocalDateTime.now());
        diary.setDiaryTime(diaryTime);
        if (StringUtils.hasText(placeInfo.getPlace())) {
            diary.setPlace(placeInfo.getPlace());
        }
        diary.setUser(user);
        diary.setGeography(placeInfo.getLongitude(), placeInfo.getLatitude());
        return diary;
    }

    private static LocalDateTime setCreateTime(final LocalDateTime dateTime, final DiaryTime diaryTime) {
        switch (diaryTime) {
            case BREAKFAST -> {
                return dateTime.withHour(8);
            }
            case BRUNCH -> {
                return dateTime.withHour(10);
            }
            case LUNCH -> {
                return dateTime.withHour(12);
            }
            case SNACK -> {
                return dateTime.withHour(14);
            }
            case LINNER -> {
                return dateTime.withHour(16);
            }
            case DINNER -> {
                return dateTime.withHour(18);
            }
            case LATESNACK -> {
                return dateTime.withHour(21);
            }
            default -> {
                return dateTime.withHour(0);
            }
        }
    }

    public void setGeography(final Double longitude, final Double latitude) {
        if(longitude == -200D && latitude == -200D) {
            return;
        }

        String pointWKT = String.format("POINT(%s %s)", longitude, latitude);
        Point point;
        try {
            point = (Point) new WKTReader().read(pointWKT);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        this.geography = point;
    }
}

