package com.fooddiary.api.entity.image;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fooddiary.api.entity.tag.Tag;
import com.fooddiary.api.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;


import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;


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

    @Enumerated(EnumType.STRING)
    private TimeStatus timeStatus;

    @Column(nullable = true, columnDefinition = "GEOMETRY")
    private Point geography;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_image_id")
    @JsonIgnore
    private DayImage dayImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_image_id")
    private Image parentImage;

    public String memo;

    @OneToMany(mappedBy = "parentImage", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Image> child = new ArrayList<>();

    @OneToMany(mappedBy = "image", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Tag> tags = new ArrayList<>();

    @PrePersist
    public void prePersist() {
        createAt = createAt == null ? LocalDateTime.now() : createAt;
    }

    public void addChildImage(Image image) {
        this.child.add(image);
        image.parentImage = this;
    }

    public void addTags(List<Tag> tags) {
        for (Tag tag : tags) {
            this.tags.add(tag);
            tag.setImage(this);
        }
    }


    public void setTimeStatus(final LocalDateTime dateTime) {
        this.timeStatus = TimeStatus.getTime(dateTime);
    }

    public void setTag(final List<Tag> tags) {
        for (Tag tag : tags) {
            this.tags.add(tag);
            tag.setImage(this);
        }
    }
    public void setDayImage(final DayImage dayImage) {
        this.dayImage = dayImage;
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


    public static Image createImage(final LocalDateTime dateTime, final String fileName, final double longitude, final double latitude, final User user) {
        final Image image = Image.builder()
                .storedFileName(fileName)
                .user(user)
                .build();
        image.setTimeStatus(dateTime);
        image.setGeography(longitude, latitude);
        return image;
    }

    public static Image createImage(final Image parentImage, final String fileName, final User user) {
        final Image image = Image.builder()
                .storedFileName(fileName)
                .user(user)
                .build();
        image.dayImage = parentImage.dayImage;
        image.parentImage = parentImage;
        image.timeStatus = parentImage.timeStatus;
        image.geography = parentImage.geography;
        return image;
    }

    public void updateMemo(final String memo) {
        this.memo = memo;
    }


    public void updateStoredImage(String storeFilename) {
        this.storedFileName = storeFilename;
    }

}
