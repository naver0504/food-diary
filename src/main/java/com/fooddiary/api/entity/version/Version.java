package com.fooddiary.api.entity.version;

import com.fooddiary.api.entity.user.User;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

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
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "version")
    private List<User> user = new ArrayList<>();

}
