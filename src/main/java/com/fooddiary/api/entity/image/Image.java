package com.fooddiary.api.entity.image;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;


@Entity
@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Image {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false)
    private String storedFileName;

    @Enumerated(EnumType.STRING)
    private TimeStatus timeStatus;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "day_image_id")
    private DayImage dayImage;


    public void setTimeStatus(final LocalDateTime dateTime) {
        this.timeStatus = TimeStatus.getTime(dateTime);
    }

    public void setDayImage(final DayImage dayImage) {
        this.dayImage = dayImage;
    }

    public static Image createImage(final LocalDateTime dateTime, final String fileName) {
        final Image image = Image.builder()
                .storedFileName(fileName)
                .build();
        image.setTimeStatus(dateTime);
        return image;
    }



}
