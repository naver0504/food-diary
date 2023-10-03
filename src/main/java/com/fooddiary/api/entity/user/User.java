package com.fooddiary.api.entity.user;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import com.fooddiary.api.entity.image.DayImage;

import org.hibernate.annotations.ColumnDefault;

import com.fooddiary.api.entity.session.Session;
import jakarta.persistence.*;
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
    @Convert(converter = RoleConverter.class)
    private Role role;
    private LocalDateTime pwUpdateAt;
    private LocalDateTime pwUpdateDelayAt;
    @Column(nullable = false)
    @ColumnDefault("0")
    private Integer pwTry;
    @Convert(converter = StatusConverter.class)
    private Status status;
    @Column(nullable = false)
    private String email;
    @Convert(converter = CreatePathConverter.class)
    private CreatePath createPath;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;
    private LocalDateTime updateAt;
    private LocalDateTime lastAccessAt;

    @OneToMany(mappedBy = "user")
    private List<Session> session = new ArrayList<>();

    @OneToMany(cascade = CascadeType.ALL, mappedBy = "user")
    private List<DayImage> dayImages = new ArrayList<>();



    @PrePersist
    public void prePersist() {
        createAt = createAt == null ? LocalDateTime.now() : createAt;
        status = status == null ? Status.ACTIVE : status;
        createPath = createPath == null ? CreatePath.NONE : createPath;
        pwTry = pwTry == null ? 0 : pwTry;
    }
}
