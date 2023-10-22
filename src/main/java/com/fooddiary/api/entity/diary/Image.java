package com.fooddiary.api.entity.diary;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fooddiary.api.entity.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.locationtech.jts.geom.Point;
import org.locationtech.jts.io.ParseException;
import org.locationtech.jts.io.WKTReader;

import java.time.LocalDateTime;


@Entity
@Getter
@Setter
@NoArgsConstructor
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

    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;
    @Column(nullable = false)
    private LocalDateTime updateAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "diary_id")
    @JsonIgnore
    private Diary diary;

    @PrePersist
    public void prePersist() {
        createAt = createAt == null ? LocalDateTime.now() : createAt;
    }


    public void setThumbnailFileName(final String thumbnailFileName) {
        this.thumbnailFileName = thumbnailFileName;
    }

    public static Image createImage(final Diary diary, final String fileName) {
        final Image image = Image.builder()
                .storedFileName(fileName)
                .diary(diary)
                .build();
        return image;
    }

    public static Image createImage(final Image parentImage, final String fileName, final User user) {
        final Image image = Image.builder()
                .storedFileName(fileName)
                .build();
        image.diary = parentImage.diary;
        return image;
    }

}
