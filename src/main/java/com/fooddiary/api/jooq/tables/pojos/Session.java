/*
 * This file is generated by jOOQ.
 */
package com.fooddiary.api.jooq.tables.pojos;


import java.io.Serializable;
import java.time.LocalDateTime;


/**
 * This class is generated by jOOQ.
 */
@SuppressWarnings({ "all", "unchecked", "rawtypes" })
public class Session implements Serializable {

    private static final long serialVersionUID = 1L;

    private String token;
    private LocalDateTime createAt;
    private LocalDateTime terminateAt;
    private String userEmail;
    private Integer userId;

    public Session() {}

    public Session(Session value) {
        this.token = value.token;
        this.createAt = value.createAt;
        this.terminateAt = value.terminateAt;
        this.userEmail = value.userEmail;
        this.userId = value.userId;
    }

    public Session(
        String token,
        LocalDateTime createAt,
        LocalDateTime terminateAt,
        String userEmail,
        Integer userId
    ) {
        this.token = token;
        this.createAt = createAt;
        this.terminateAt = terminateAt;
        this.userEmail = userEmail;
        this.userId = userId;
    }

    /**
     * Getter for <code>my_food_diarybook.session.token</code>.
     */
    public String getToken() {
        return this.token;
    }

    /**
     * Setter for <code>my_food_diarybook.session.token</code>.
     */
    public void setToken(String token) {
        this.token = token;
    }

    /**
     * Getter for <code>my_food_diarybook.session.create_at</code>.
     */
    public LocalDateTime getCreateAt() {
        return this.createAt;
    }

    /**
     * Setter for <code>my_food_diarybook.session.create_at</code>.
     */
    public void setCreateAt(LocalDateTime createAt) {
        this.createAt = createAt;
    }

    /**
     * Getter for <code>my_food_diarybook.session.terminate_at</code>.
     */
    public LocalDateTime getTerminateAt() {
        return this.terminateAt;
    }

    /**
     * Setter for <code>my_food_diarybook.session.terminate_at</code>.
     */
    public void setTerminateAt(LocalDateTime terminateAt) {
        this.terminateAt = terminateAt;
    }

    /**
     * Getter for <code>my_food_diarybook.session.user_email</code>.
     */
    public String getUserEmail() {
        return this.userEmail;
    }

    /**
     * Setter for <code>my_food_diarybook.session.user_email</code>.
     */
    public void setUserEmail(String userEmail) {
        this.userEmail = userEmail;
    }

    /**
     * Getter for <code>my_food_diarybook.session.user_id</code>.
     */
    public Integer getUserId() {
        return this.userId;
    }

    /**
     * Setter for <code>my_food_diarybook.session.user_id</code>.
     */
    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        final Session other = (Session) obj;
        if (this.token == null) {
            if (other.token != null)
                return false;
        }
        else if (!this.token.equals(other.token))
            return false;
        if (this.createAt == null) {
            if (other.createAt != null)
                return false;
        }
        else if (!this.createAt.equals(other.createAt))
            return false;
        if (this.terminateAt == null) {
            if (other.terminateAt != null)
                return false;
        }
        else if (!this.terminateAt.equals(other.terminateAt))
            return false;
        if (this.userEmail == null) {
            if (other.userEmail != null)
                return false;
        }
        else if (!this.userEmail.equals(other.userEmail))
            return false;
        if (this.userId == null) {
            if (other.userId != null)
                return false;
        }
        else if (!this.userId.equals(other.userId))
            return false;
        return true;
    }

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((this.token == null) ? 0 : this.token.hashCode());
        result = prime * result + ((this.createAt == null) ? 0 : this.createAt.hashCode());
        result = prime * result + ((this.terminateAt == null) ? 0 : this.terminateAt.hashCode());
        result = prime * result + ((this.userEmail == null) ? 0 : this.userEmail.hashCode());
        result = prime * result + ((this.userId == null) ? 0 : this.userId.hashCode());
        return result;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("Session (");

        sb.append(token);
        sb.append(", ").append(createAt);
        sb.append(", ").append(terminateAt);
        sb.append(", ").append(userEmail);
        sb.append(", ").append(userId);

        sb.append(")");
        return sb.toString();
    }
}
