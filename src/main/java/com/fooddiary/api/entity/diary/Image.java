package com.fooddiary.api.entity.diary;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonIgnore;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;


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
    private Long id;

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
        final Image image = builder()
                .storedFileName(fileName)
                .diary(diary)
                .build();
        return image;
    }

    public static Image createImage(final Image parentImage, final String fileName) {
        final Image image = builder()
                .storedFileName(fileName)
                .build();
        image.diary = parentImage.diary;
        return image;
    }

}
