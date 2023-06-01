package com.fooddiary.api.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.sql.Timestamp;

@Entity
@Getter
@Setter
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;
    private String pw;
    @NonNull
    private String name;
    @NonNull
    private String email;
    @NonNull
    private CreatePath createPath;
    private Timestamp createAt;
    private Timestamp updateAt;
}
