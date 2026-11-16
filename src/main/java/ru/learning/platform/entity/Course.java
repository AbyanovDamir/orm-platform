package ru.learning.platform.entity;

import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.ArrayList;
import java.util.Set;
import java.util.LinkedHashSet;

@Entity
@Table(name = "courses")
public class Course extends BaseEntity {
    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 4000)
    private String description;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "category_id", nullable = false)
    private Category category;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "teacher_id", nullable = false)
    private User teacher;

    @Column(length = 60)
    private String duration;

    @Column(nullable = false)
    private LocalDate startDate;

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("orderIndex ASC, id ASC")
    private List<CourseModule> modules = new ArrayList<>();

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Enrollment> enrollments = new ArrayList<>();

    @OneToMany(mappedBy = "course", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<CourseReview> reviews = new ArrayList<>();

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(name = "course_tag", joinColumns = @JoinColumn(name = "course_id"),
            inverseJoinColumns = @JoinColumn(name = "tag_id"))
    private Set<Tag> tags = new LinkedHashSet<>();

    protected Course() {
    }

    public Course(String title, String description, Category category, User teacher, String duration,
            LocalDate startDate) {
        this.title = title;
        this.description = description;
        this.category = category;
        this.teacher = teacher;
        this.duration = duration;
        this.startDate = startDate;
    }

    public String getTitle() {
        return title;
    }

    public String getDescription() {
        return description;
    }

    public Category getCategory() {
        return category;
    }

    public User getTeacher() {
        return teacher;
    }

    public String getDuration() {
        return duration;
    }

    public LocalDate getStartDate() {
        return startDate;
    }

    public List<CourseModule> getModules() {
        return modules;
    }

    public List<Enrollment> getEnrollments() {
        return enrollments;
    }

    public List<CourseReview> getReviews() {
        return reviews;
    }

    public Set<Tag> getTags() {
        return tags;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setCategory(Category category) {
        this.category = category;
    }

    public void setTeacher(User teacher) {
        this.teacher = teacher;
    }

    public void setDuration(String duration) {
        this.duration = duration;
    }

    public void setStartDate(LocalDate startDate) {
        this.startDate = startDate;
    }
}
