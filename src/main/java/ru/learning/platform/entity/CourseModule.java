package ru.learning.platform.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "modules")
public class CourseModule extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false)
    private int orderIndex;

    @Column(length = 4000)
    private String description;

    @OneToMany(mappedBy = "module", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Lesson> lessons = new ArrayList<>();

    @OneToOne(mappedBy = "module", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    private Quiz quiz;

    protected CourseModule() {
    }

    public CourseModule(Course course, String title, int orderIndex, String description) {
        this.course = course;
        this.title = title;
        this.orderIndex = orderIndex;
        this.description = description;
    }

    public Course getCourse() {
        return course;
    }

    public String getTitle() {
        return title;
    }

    public int getOrderIndex() {
        return orderIndex;
    }

    public String getDescription() {
        return description;
    }

    public List<Lesson> getLessons() {
        return lessons;
    }

    public Quiz getQuiz() {
        return quiz;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setOrderIndex(int orderIndex) {
        this.orderIndex = orderIndex;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
