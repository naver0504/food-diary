package com.fooddiary.api.entity.version;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
public class Version {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    @Column(nullable = false)
    private String version;
    @Column(nullable = false)
    private LocalDateTime releaseAt;
    private boolean isRelease;
    @Column(updatable = false)
    private LocalDateTime createdAt;


}
