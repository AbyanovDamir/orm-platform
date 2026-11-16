package ru.learning.platform.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "quizzes")
public class Quiz extends BaseEntity {
    @OneToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false, unique = true)
    private CourseModule module;

    @Column(nullable = false, length = 160)
    private String title;

    private Integer timeLimit;

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Question> questions = new ArrayList<>();

    @OneToMany(mappedBy = "quiz", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<QuizSubmission> results = new ArrayList<>();

    protected Quiz() {
    }

    public Quiz(CourseModule module, String title, Integer timeLimit) {
        this.module = module;
        this.title = title;
        this.timeLimit = timeLimit;
    }

    public CourseModule getModule() {
        return module;
    }

    public String getTitle() {
        return title;
    }

    public Integer getTimeLimit() {
        return timeLimit;
    }

    public List<Question> getQuestions() {
        return questions;
    }

    public List<QuizSubmission> getResults() {
        return results;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setTimeLimit(Integer timeLimit) {
        this.timeLimit = timeLimit;
    }
}
