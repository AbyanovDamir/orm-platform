package ru.learning.platform.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "lesson_sessions")
public class LessonSession extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false)
    private Instant startsAt;

    @Column(nullable = false)
    private Instant endsAt;

    @Column(nullable = false, length = 300)
    private String location;

    protected LessonSession() {
    }

    public LessonSession(Lesson lesson, Instant startsAt, Instant endsAt, String location) {
        this.lesson = lesson;
        this.startsAt = startsAt;
        this.endsAt = endsAt;
        this.location = location;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public Instant getStartsAt() {
        return startsAt;
    }

    public Instant getEndsAt() {
        return endsAt;
    }

    public String getLocation() {
        return location;
    }

    public void setStartsAt(Instant startsAt) {
        this.startsAt = startsAt;
    }

    public void setEndsAt(Instant endsAt) {
        this.endsAt = endsAt;
    }

    public void setLocation(String location) {
        this.location = location;
    }
}
