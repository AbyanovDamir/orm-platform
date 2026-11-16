package ru.learning.platform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "tags")
public class Tag extends BaseEntity {
    @Column(nullable = false, length = 80, unique = true)
    private String name;

    protected Tag() {
    }

    public Tag(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
