package com.fooddiary.api.entity.image;


import com.fooddiary.api.entity.user.User;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class DayImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "day_image_id")
    private Integer id;

    @Embedded
    private Time time;

    @OneToMany(mappedBy = "dayImage", cascade = CascadeType.ALL)
    private List<Image> images = new ArrayList<>();


    private String thumbNailImagePath;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    public static DayImage createDayImage(final List<Image> images, final LocalDateTime dateTime) {

        DayImage dayImage = DayImage.builder()
                .time(new Time(dateTime))
                .images(images)
                .build();

        dayImage.updateThumbNailImageName(images.get(0).getStoredFileName());
        return dayImage;
    }

    public void updateThumbNailImageName(final String thumbNailImagePath) {
        this.thumbNailImagePath = thumbNailImagePath;
    }

    public void setImages(final List<Image> images) {
        for (Image image : images) {
            this.images.add(image);
            image.setDayImage(this);
        }

    }

    public void setUser(final User user) {
        this.user = user;
        user.getDayImages().add(this);
    }


}
