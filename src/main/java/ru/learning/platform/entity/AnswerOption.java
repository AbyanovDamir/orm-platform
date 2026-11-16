package ru.learning.platform.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "answer_options")
public class AnswerOption extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(nullable = false, length = 1000)
    private String text;

    @Column(nullable = false, name = "is_correct")
    private boolean correct;

    protected AnswerOption() {
    }

    public AnswerOption(Question question, String text, boolean correct) {
        this.question = question;
        this.text = text;
        this.correct = correct;
    }

    public Question getQuestion() {
        return question;
    }

    public String getText() {
        return text;
    }

    public boolean isCorrect() {
        return correct;
    }
}
