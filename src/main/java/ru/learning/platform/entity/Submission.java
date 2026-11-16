package ru.learning.platform.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "submissions", uniqueConstraints = @UniqueConstraint(name = "uq_submissions_pair",
        columnNames = {"student_id", "assignment_id"}))
public class Submission extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "assignment_id", nullable = false)
    private Assignment assignment;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private Instant submittedAt;

    @Column(nullable = false, length = 10000)
    private String content;

    private Integer score;

    @Column(length = 4000)
    private String feedback;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private SubmissionStatus status;

    protected Submission() {
    }

    public Submission(Assignment assignment, User student, Instant submittedAt, String content, Integer score,
            String feedback, SubmissionStatus status) {
        this.assignment = assignment;
        this.student = student;
        this.submittedAt = submittedAt;
        this.content = content;
        this.score = score;
        this.feedback = feedback;
        this.status = status;
    }

    public Assignment getAssignment() {
        return assignment;
    }

    public User getStudent() {
        return student;
    }

    public Instant getSubmittedAt() {
        return submittedAt;
    }

    public String getContent() {
        return content;
    }

    public Integer getScore() {
        return score;
    }

    public String getFeedback() {
        return feedback;
    }

    public SubmissionStatus getStatus() {
        return status;
    }

    public void setScore(Integer score) {
        this.score = score;
    }

    public void setFeedback(String feedback) {
        this.feedback = feedback;
    }

    public void setStatus(SubmissionStatus status) {
        this.status = status;
    }
}
