package com.fooddiary.api.entity.session;

import com.fooddiary.api.entity.user.User;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity
@Getter
@Setter
public class Session {
    @Id
    private String token;
    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;
    @Column(nullable = false, updatable = false)
    private LocalDateTime createAt;
    private LocalDateTime terminateAt;

    @PrePersist
    public void prePersist() {
        createAt = createAt == null ? LocalDateTime.now() : createAt;
    }
}
