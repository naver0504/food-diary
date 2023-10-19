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

    @Embedded
    private Time time;

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
        diary.setTime(new Time(dateTime));
        diary.setCreateAt(LocalDateTime.now());
        diary.setDiaryTime(diaryTime);
        if (StringUtils.hasText(placeInfo.getPlace())) {
            diary.setPlace(placeInfo.getPlace());
        }
        diary.setUser(user);
        diary.setGeography(placeInfo.getLongitude(), placeInfo.getLatitude());
        return diary;
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

