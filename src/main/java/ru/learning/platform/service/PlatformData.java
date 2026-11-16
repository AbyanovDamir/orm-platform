package ru.learning.platform.service;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import ru.learning.platform.entity.*;

import java.time.Instant;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.Set;

public final class PlatformData {
    private PlatformData() {
    }

    public record UserInput(@NotBlank @Size(max=120) String name, @NotBlank @Email @Size(max=254) String email,
            @NotNull Role role) {
    }

    public record UserView(Long id, String name, String email, Role role) {
    }

    public record ProfileInput(@Size(max=4000) String bio, @Size(max=500) String avatarUrl) {
    }

    public record ProfileView(Long id, Long userId, String bio, String avatarUrl) {
    }

    public record CategoryInput(@NotBlank @Size(max=120) String name) {
    }

    public record TagInput(@NotBlank @Size(max=80) String name) {
    }

    public record NamedView(Long id, String name) {
    }

    public record CourseInput(@NotBlank @Size(max=160) String title,
            @NotBlank @Size(max=4000) String description, @NotNull @Positive Long categoryId,
            @NotNull @Positive Long teacherId, @Size(max=60) String duration, @NotNull LocalDate startDate,
            @NotNull @Size(max=30) Set<@NotNull @Positive Long> tagIds) {
    }

    public record CourseView(Long id, String title, String description, Long categoryId, Long teacherId,
            String duration, LocalDate startDate, List<NamedView> tags) {
    }

    public record CourseDetail(CourseView course, List<ModuleDetail> modules) {
    }

    public record ModuleInput(@NotBlank @Size(max=160) String title, @Positive int orderIndex,
            @Size(max=4000) String description) {
    }

    public record ModuleView(Long id, Long courseId, String title, int orderIndex, String description) {
    }

    public record ModuleDetail(ModuleView module, List<LessonDetail> lessons, Long quizId) {
    }

    public record LessonInput(@NotBlank @Size(max=160) String title, @NotBlank @Size(max=10000) String content,
            @Size(max=500) String videoUrl) {
    }

    public record LessonView(Long id, Long moduleId, String title, String content, String videoUrl) {
    }

    public record LessonDetail(LessonView lesson, List<AssignmentView> assignments, List<SessionView> sessions) {
    }

    public record SessionInput(@NotNull Instant startsAt, @NotNull Instant endsAt,
            @NotBlank @Size(max=300) String location) {
    }

    public record SessionView(Long id, Long lessonId, Instant startsAt, Instant endsAt, String location) {
    }

    public record AssignmentInput(@NotBlank @Size(max=160) String title,
            @NotBlank @Size(max=4000) String description, @NotNull LocalDate dueDate, @Positive int maxScore) {
    }

    public record AssignmentView(Long id, Long lessonId, String title, String description, LocalDate dueDate,
            int maxScore) {
    }

    public record EnrollmentView(Long id, Long courseId, String courseTitle, Long studentId, String studentName,
            LocalDate enrollDate, EnrollmentStatus status) {
    }

    public record SubmissionInput(@NotNull @Positive Long studentId, @NotBlank @Size(max=10000) String content) {
    }

    public record GradeInput(@NotNull @Positive Long teacherId, @NotNull @PositiveOrZero Integer score,
            @Size(max=4000) String feedback) {
    }

    public record SubmissionView(Long id, Long assignmentId, Long studentId, Instant submittedAt, String content,
            Integer score, String feedback, SubmissionStatus status) {
    }

    public record QuizInput(@NotBlank @Size(max=160) String title, @Positive Integer timeLimit,
            @NotEmpty @Size(max=100) List<@NotNull @Valid QuestionInput> questions) {
    }

    public record QuizMetadata(@NotBlank @Size(max=160) String title, @Positive Integer timeLimit) {
    }

    public record QuestionInput(@NotBlank @Size(max=2000) String text, @NotNull QuestionType type,
            @NotNull @Size(min=2,max=30) List<@NotNull @Valid OptionInput> options) {
    }

    public record OptionInput(@NotBlank @Size(max=1000) String text, boolean correct) {
    }

    public record QuizView(Long id, Long moduleId, String title, Integer timeLimit, List<QuestionView> questions) {
    }

    public record QuestionView(Long id, String text, QuestionType type, List<OptionView> options) {
    }

    public record OptionView(Long id, String text) {
    }

    public record AttemptInput(@NotNull @Positive Long studentId,
            @NotEmpty @Size(max=100) Map<@NotNull @Positive Long,
            @NotEmpty @Size(max=30) Set<@NotNull @Positive Long>> answers) {
    }

    public record AttemptView(Long id, Long quizId, Long studentId, int score, int maxScore, Instant takenAt) {
    }

    public record ReviewInput(@NotNull @Positive Long studentId, @Min(1) @Max(5) int rating,
            @Size(max=4000) String comment) {
    }

    public record ReviewView(Long id, Long courseId, Long studentId, int rating, String comment,
            Instant createdAt) {
    }
}
