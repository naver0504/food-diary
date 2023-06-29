package com.fooddiary.api.entity.image;

import com.fooddiary.api.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.ToString;

import java.time.LocalDateTime;

@Entity
@Getter
@ToString
public class Image {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String storedFileName;

    @Enumerated(EnumType.STRING)
    private TimeStatus timeStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_image_id")
    private DayImage dayImage;

    public void setTimeStatus(LocalDateTime dateTime) {
        this.timeStatus = TimeStatus.getTime(dateTime);
    }

    public static Image createImage(LocalDateTime dateTime, String fileName) {
        Image image = new Image();
        image.setTimeStatus(dateTime);
        image.storedFileName = fileName;
        return image;
    }



}
