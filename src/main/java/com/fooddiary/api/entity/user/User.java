package com.fooddiary.api.entity.user;

import java.sql.Timestamp;
import java.time.LocalDateTime;

import jakarta.persistence.Column;
import jakarta.persistence.Convert;
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
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String pw;
    @Column(nullable = false)
    private String name;
    @Convert(converter = StatusConverter.class)
    private Status status;
    @Column(nullable = false)
    private String email;
    @Convert(converter = CreatePathConverter.class)
    private CreatePath createPath;
    @Column(nullable = false, updatable = false)
    private Timestamp createAt;
    private Timestamp updateAt;

    @PrePersist
    public void prePersist() {
        createAt = createAt == null ? Timestamp.valueOf(LocalDateTime.now()) : createAt;
        status = status == null ? Status.ACTIVE : status;
        createPath = createPath == null ? CreatePath.NONE : createPath;
    }
}
