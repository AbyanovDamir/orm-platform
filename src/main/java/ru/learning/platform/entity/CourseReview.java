package ru.learning.platform.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "course_reviews", uniqueConstraints = @UniqueConstraint(name = "uq_course_reviews_pair",
        columnNames = {"student_id", "course_id"}))
public class CourseReview extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private int rating;

    @Column(length = 4000)
    private String comment;

    @Column(nullable = false)
    private Instant createdAt;

    protected CourseReview() {
    }

    public CourseReview(Course course, User student, int rating, String comment, Instant createdAt) {
        this.course = course;
        this.student = student;
        this.rating = rating;
        this.comment = comment;
        this.createdAt = createdAt;
    }

    public Course getCourse() {
        return course;
    }

    public User getStudent() {
        return student;
    }

    public int getRating() {
        return rating;
    }

    public String getComment() {
        return comment;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
