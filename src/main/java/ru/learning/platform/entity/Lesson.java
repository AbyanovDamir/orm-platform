package ru.learning.platform.entity;

import jakarta.persistence.*;
import java.util.List;
import java.util.ArrayList;

@Entity
@Table(name = "lessons")
public class Lesson extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "module_id", nullable = false)
    private CourseModule module;

    @Column(nullable = false, length = 160)
    private String title;

    @Column(nullable = false, length = 10000)
    private String content;

    @Column(length = 500)
    private String videoUrl;

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("id ASC")
    private List<Assignment> assignments = new ArrayList<>();

    @OneToMany(mappedBy = "lesson", fetch = FetchType.LAZY,
            cascade = CascadeType.ALL, orphanRemoval = true)
    @OrderBy("startsAt ASC, id ASC")
    private List<LessonSession> sessions = new ArrayList<>();

    protected Lesson() {
    }

    public Lesson(CourseModule module, String title, String content, String videoUrl) {
        this.module = module;
        this.title = title;
        this.content = content;
        this.videoUrl = videoUrl;
    }

    public CourseModule getModule() {
        return module;
    }

    public String getTitle() {
        return title;
    }

    public String getContent() {
        return content;
    }

    public String getVideoUrl() {
        return videoUrl;
    }

    public List<Assignment> getAssignments() {
        return assignments;
    }

    public List<LessonSession> getSessions() {
        return sessions;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setContent(String content) {
        this.content = content;
    }

    public void setVideoUrl(String videoUrl) {
        this.videoUrl = videoUrl;
    }
}
