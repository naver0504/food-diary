package com.fooddiary.api.entity.image;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fooddiary.api.dto.request.SaveImageRequestDTO;
import com.fooddiary.api.entity.diary.Diary;
import com.fooddiary.api.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.time.LocalDateTime;


@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@ToString
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String storedFileName;
    @Column(nullable = false)
    private String thumbnailFileName;

    @Enumerated(EnumType.STRING)
    private DiaryTime diaryTime;

    @Column(nullable = true, columnDefinition = "GEOMETRY")
    private Point geography;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id")
    @JsonIgnore
    private Diary diary;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    @PrePersist
    public void prePersist() {
        createAt = createAt == null ? LocalDateTime.now() : createAt;
    }

    public void setTimeStatus(DiaryTime diaryTime) {
        this.diaryTime = diaryTime;
    }

    public void setDayImage(final DayImage dayImage) {
        this.diary = diary;
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


    public static Image createImage(final String fileName, final SaveImageRequestDTO saveImageRequestDTO, final User user) {
        final Image image = Image.builder()
                .storedFileName(fileName)
                .user(user)
                .build();
        image.setTimeStatus(saveImageRequestDTO.getDiaryTime());
        image.setGeography(saveImageRequestDTO.getLongitude(), saveImageRequestDTO.getLatitude());
        return image;
    }

    public static Image createImage(final Image parentImage, final String fileName, final User user) {
        final Image image = Image.builder()
                .storedFileName(fileName)
                .user(user)
                .build();
        image.diary = parentImage.diary;
        image.geography = parentImage.geography;
        return image;
    }

    public void updateStoredImage(String storeFilename) {
        this.storedFileName = storeFilename;
    }

}
