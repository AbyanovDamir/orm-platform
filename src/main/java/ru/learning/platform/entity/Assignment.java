package ru.learning.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "assignments")
public class Assignment extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @Column(nullable = false)
    private LocalDate dueDate;

    @Column(nullable = false)
    private int maxScore;

    @OneToMany(mappedBy = "assignment", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Submission> submissions = new ArrayList<>();

    protected Assignment() {
    }

    public Assignment(Lesson lesson, String title, String description, LocalDate dueDate, int maxScore) {
        this.lesson = lesson;
        this.title = title;
        this.description = description;
        this.dueDate = dueDate;
        this.maxScore = maxScore;
    }

    public Lesson getLesson() {
        return lesson;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public LocalDate getDueDate() {
        return dueDate;
    }

    public int getMaxScore() {
        return maxScore;
    }

    public List<Submission> getSubmissions() {
        return submissions;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDueDate(LocalDate dueDate) {
        this.dueDate = dueDate;
    }

    public void setMaxScore(int maxScore) {
        this.maxScore = maxScore;
    }
}
