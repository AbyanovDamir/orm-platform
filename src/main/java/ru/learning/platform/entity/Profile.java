package ru.learning.platform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "profiles")
public class Profile extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false, unique = true)
    private User user;

    @Column(length = 4000)
    private String bio;

    @Column(length = 500)
    private String avatarUrl;

    protected Profile() {
    }

    public Profile(User user, String bio, String avatarUrl) {
        this.user = user;
        this.bio = bio;
        this.avatarUrl = avatarUrl;
    }

    public User getUser() {
        return user;
    }

    public String getBio() {
        return bio;
    }

    public String getAvatarUrl() {
        return avatarUrl;
    }

    public void setBio(String bio) {
        this.bio = bio;
    }

    public void setAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
