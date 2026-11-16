package ru.learning.platform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User extends BaseEntity {
    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, length = 254, unique = true)
    private String email;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @OneToOne(mappedBy = "user", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Profile profile;

    protected User() {
    }

    public User(String name, String email, Role role) {
        this.name = name;
        this.email = email;
        this.role = role;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public Role getRole() {
        return role;
    }

    public Profile getProfile() {
        return profile;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
