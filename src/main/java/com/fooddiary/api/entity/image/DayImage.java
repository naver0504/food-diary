package com.fooddiary.api.entity.image;


import com.fooddiary.api.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.geolatte.geom.Point;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DayImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "day_image_id")
    private Integer id;

    @Embedded
    private Time time;

    //@OneToMany(mappedBy = "dayImage", cascade = CascadeType.ALL)
    //@Builder.Default
    //private List<Image> images = new ArrayList<>();


    private String thumbNailImagePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;

    @Column(nullable = true, columnDefinition = "GEOMETRY")
    private Point geography;


    public static DayImage createDayImage(final List<Image> images, final LocalDateTime dateTime, final User user) {

        final DayImage dayImage = DayImage.builder()
                .time(new Time(dateTime))
                .user(user)
                .build();

        dayImage.updateThumbNailImageName(images.get(0).getStoredFileName());
        dayImage.setImages(images);
        return dayImage;
    }

    public void updateThumbNailImageName(final String thumbNailImagePath) {
        this.thumbNailImagePath = thumbNailImagePath;
    }

    public void setImages(final List<Image> images) {
        for (Image image : images) {
           // this.images.add(image);
            image.setDayImage(this);
        }
    }

    public void setUser(final User user) {
        this.user = user;
        user.getDayImages().add(this);
    }


}
