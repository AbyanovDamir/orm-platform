package ru.learning.platform.entity;

import jakarta.persistence.*;
import java.time.Instant;

@Entity
@Table(name = "quiz_submissions")
public class QuizSubmission extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private User student;

    @Column(nullable = false)
    private int score;

    @Column(nullable = false)
    private int maxScore;

    @Column(nullable = false)
    private Instant takenAt;

    protected QuizSubmission() {
    }

    public QuizSubmission(Quiz quiz, User student, int score, int maxScore, Instant takenAt) {
        this.quiz = quiz;
        this.student = student;
        this.score = score;
        this.maxScore = maxScore;
        this.takenAt = takenAt;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public User getStudent() {
        return student;
    }

    public int getScore() {
        return score;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public Instant getTakenAt() {
        return takenAt;
    }
}
