package ru.learning.platform.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "questions")
public class Question extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(nullable = false, length = 2000)
    private String text;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private QuestionType type;

    @OneToMany(mappedBy = "question", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<AnswerOption> options = new ArrayList<>();

    protected Question() {
    }

    public Question(Quiz quiz, String text, QuestionType type) {
        this.quiz = quiz;
        this.text = text;
        this.type = type;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public String getText() {
        return text;
    }

    public QuestionType getType() {
        return type;
    }

    public List<AnswerOption> getOptions() {
        return options;
    }
}
