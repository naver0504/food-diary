package com.fooddiary.api.entity.version;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
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
    private Integer id;
    @Column(nullable = false)
    private String version;
    @Column(nullable = false)
    private LocalDateTime releaseAt;
    private boolean isRelease;
    @Column(updatable = false)
    private LocalDateTime createdAt;


}
