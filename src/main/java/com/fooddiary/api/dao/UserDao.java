package com.fooddiary.api.dao;

import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

import java.sql.Timestamp;

@Getter
@Setter
@ToString
public class UserDao {
    private Integer id;
    private String token;
    private String userEmail;
    private Timestamp crateAt;
    private Timestamp updateAt;
}
