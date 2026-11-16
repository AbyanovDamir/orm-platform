package ru.learning.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(name = "enrollments", uniqueConstraints = @UniqueConstraint(name = "uq_enrollments_pair",
        columnNames = {"user_id", "course_id"}))
public class Enrollment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User student;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false)
    private LocalDate enrollDate;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private EnrollmentStatus status;

    protected Enrollment() {
    }

    public Enrollment(User student, Course course, LocalDate enrollDate, EnrollmentStatus status) {
        this.student = student;
        this.course = course;
        this.enrollDate = enrollDate;
        this.status = status;
    }

    public User getStudent() {
        return student;
    }

    public Course getCourse() {
        return course;
    }

    public LocalDate getEnrollDate() {
        return enrollDate;
    }

    public EnrollmentStatus getStatus() {
        return status;
    }

    public void setStatus(EnrollmentStatus status) {
        this.status = status;
    }
}
