package com.fooddiary.api.entity.diary;


import com.fooddiary.api.dto.request.diary.PlaceInfoDTO;
import com.fooddiary.api.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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
    private Long id;

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

    @Column(length = 500)
    private String memo;

    @OneToMany(mappedBy = "diary", cascade = CascadeType.ALL, fetch = FetchType.LAZY)
    private List<DiaryTag> diaryTags = new ArrayList<>();

    public static Diary createDiary(final LocalDate dateTime, final User user, final DiaryTime diaryTime, final PlaceInfoDTO placeInfo) {
        final Diary diary = new Diary();
        diary.setCreateTime(makeCreateTime(dateTime, diaryTime));
        diary.setCreateAt(LocalDateTime.now());
        diary.setDiaryTime(diaryTime);
        diary.setUser(user);
        if (placeInfo != null) {
            if (StringUtils.hasText(placeInfo.getPlace())) {
                diary.setPlace(placeInfo.getPlace());
            }
            diary.setGeography(placeInfo.getLongitude(), placeInfo.getLatitude());
        }
        return diary;
    }

    public static LocalDateTime makeCreateTime(final LocalDate dateTime, final DiaryTime diaryTime) {
        switch (diaryTime) {
            case BREAKFAST -> {
                return dateTime.atTime(LocalTime.of(8, 0));
            }
            case BRUNCH -> {
                return dateTime.atTime(LocalTime.of(10, 0));
            }
            case LUNCH -> {
                return dateTime.atTime(LocalTime.of(12, 0));
            }
            case SNACK -> {
                return dateTime.atTime(LocalTime.of(14, 0));
            }
            case LINNER -> {
                return dateTime.atTime(LocalTime.of(16, 0));
            }
            case DINNER -> {
                return dateTime.atTime(LocalTime.of(18, 0));
            }
            case LATESNACK -> {
                return dateTime.atTime(LocalTime.of(21, 0));
            }
            default -> {
                return dateTime.atStartOfDay();
            }
        }
    }

    public void setGeography(final Double longitude, final Double latitude) {
        if(longitude.equals(-200D) && latitude.equals(-200D)) {
            return;
        }

        final String pointWKT = String.format("POINT(%s %s)", longitude, latitude);
        final Point point;
        try {
            point = (Point) new WKTReader().read(pointWKT);
        } catch (ParseException e) {
            throw new RuntimeException(e);
        }
        geography = point;
    }
}

