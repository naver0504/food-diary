package com.fooddiary.api.entity.image;


import com.fooddiary.api.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
public class DayImage {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "day_image_id")
    private Integer id;

    @Embedded
    private Time time;

    @OneToMany(mappedBy = "dayImage", cascade = CascadeType.ALL)
    private List<Image> images = new ArrayList<>();

    @OneToOne(fetch = FetchType.LAZY, cascade = CascadeType.ALL)
    @JoinColumn(name = "thumb_nail_image_id")
    private Image thumbNailImage;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id")
    private User user;


    public static DayImage createDayImage(List<Image> images, LocalDateTime dateTime) {
        DayImage dayImage = new DayImage();
        dayImage.time = new Time(dateTime);
        dayImage.images = images;
        dayImage.thumbNailImage = images.get(0);
        return dayImage;
    }


}
