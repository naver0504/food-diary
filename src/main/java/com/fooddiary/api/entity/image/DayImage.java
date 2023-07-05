package com.fooddiary.api.entity.image;


import com.fooddiary.api.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@ToString
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


    public static DayImage createDayImage(List<Image> images, LocalDateTime dateTime) {
        DayImage dayImage = new DayImage();
        dayImage.time = new Time(dateTime);
        dayImage.setImages(images);
        dayImage.setThumbNailImagePath(images.get(0).getStoredFileName());
        return dayImage;
    }

    public void setImages(List<Image> images) {
        for (Image image : images) {
            this.images.add(image);
            image.setDayImage(this);
        }

    }

    public void setUser(User user) {
        this.user = user;
        user.getDayImages().add(this);

    }


}
