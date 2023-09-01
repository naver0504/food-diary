package com.fooddiary.api.entity.notice;

import java.time.LocalDate;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import lombok.Getter;
import lombok.Setter;

@Entity
@Getter
@Setter
public class Notice {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String title;
    private String content;
    @Column(nullable = false, updatable = false)
    private LocalDate createAt;
    private boolean available;

    @PrePersist
    public void prePersist() {
        createAt = createAt == null ? LocalDate.now() : createAt;
    }
}