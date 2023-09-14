package com.fooddiary.api.entity.notice;

import java.time.LocalDate;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Component;

@Entity
@Getter
@Setter
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private String content;
    @Column(nullable = false)
    private LocalDate noticeAt;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private boolean available;
    @Column(nullable = false)
    private Integer createUserId;
    private Integer updateUserId;

    @PrePersist
    public void prePersist() {
        createAt = createAt == null ? LocalDateTime.now() : createAt;
    }
}
