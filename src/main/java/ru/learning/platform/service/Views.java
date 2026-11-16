package ru.learning.platform.service;

import ru.learning.platform.entity.*;

import java.util.Comparator;

import static ru.learning.platform.service.PlatformData.*;

final class Views {
    private Views() {
    }

    static UserView user(User entity) {
        return new UserView(entity.getId(), entity.getName(), entity.getEmail(), entity.getRole());
    }

    static ProfileView profile(Profile entity) {
        return new ProfileView(entity.getId(), entity.getUser().getId(), entity.getBio(),
                entity.getAvatarUrl());
    }

    static NamedView category(Category entity) {
        return new NamedView(entity.getId(), entity.getName());
    }

    static NamedView tag(Tag entity) {
        return new NamedView(entity.getId(), entity.getName());
    }

    static ModuleView module(CourseModule entity) {
        return new ModuleView(entity.getId(), entity.getCourse().getId(), entity.getTitle(),
                entity.getOrderIndex(), entity.getDescription());
    }

    static LessonView lesson(Lesson entity) {
        return new LessonView(entity.getId(), entity.getModule().getId(), entity.getTitle(),
                entity.getContent(), entity.getVideoUrl());
    }

    static SessionView session(LessonSession entity) {
        return new SessionView(entity.getId(), entity.getLesson().getId(), entity.getStartsAt(),
                entity.getEndsAt(), entity.getLocation());
    }

    static AssignmentView assignment(Assignment entity) {
        return new AssignmentView(entity.getId(), entity.getLesson().getId(), entity.getTitle(),
                entity.getDescription(), entity.getDueDate(), entity.getMaxScore());
    }

    static EnrollmentView enrollment(Enrollment entity) {
        return new EnrollmentView(entity.getId(), entity.getCourse().getId(), entity.getCourse().getTitle(),
                entity.getStudent().getId(), entity.getStudent().getName(), entity.getEnrollDate(),
                entity.getStatus());
    }

    static SubmissionView submission(Submission entity) {
        return new SubmissionView(entity.getId(), entity.getAssignment().getId(), entity.getStudent().getId(),
                entity.getSubmittedAt(), entity.getContent(), entity.getScore(), entity.getFeedback(),
                entity.getStatus());
    }

    static AttemptView attempt(QuizSubmission entity) {
        return new AttemptView(entity.getId(), entity.getQuiz().getId(), entity.getStudent().getId(),
                entity.getScore(), entity.getMaxScore(), entity.getTakenAt());
    }

    static ReviewView review(CourseReview entity) {
        return new ReviewView(entity.getId(), entity.getCourse().getId(), entity.getStudent().getId(),
                entity.getRating(), entity.getComment(), entity.getCreatedAt());
    }

    static CourseView course(Course course) {
        return new CourseView(course.getId(), course.getTitle(), course.getDescription(),
                course.getCategory().getId(), course.getTeacher().getId(), course.getDuration(),
                course.getStartDate(), course.getTags().stream().map(Views::tag)
                        .sorted(Comparator.comparing(NamedView::name)).toList());
    }

    static CourseDetail courseDetail(Course course) {
        return new CourseDetail(course(course), course.getModules().stream().map(Views::moduleDetail).toList());
    }

    private static ModuleDetail moduleDetail(CourseModule module) {
        Quiz quiz = module.getQuiz();
        return new ModuleDetail(module(module), module.getLessons().stream().map(Views::lessonDetail).toList(),
                quiz == null ? null : quiz.getId());
    }

    private static LessonDetail lessonDetail(Lesson lesson) {
        return new LessonDetail(lesson(lesson), lesson.getAssignments().stream().map(Views::assignment).toList(),
                lesson.getSessions().stream().map(Views::session).toList());
    }

    static QuizView quiz(Quiz quiz) {
        return new QuizView(quiz.getId(), quiz.getModule().getId(), quiz.getTitle(), quiz.getTimeLimit(),
                quiz.getQuestions().stream().map(question -> new QuestionView(question.getId(), question.getText(),
                        question.getType(), question.getOptions().stream()
                                .map(option -> new OptionView(option.getId(), option.getText())).toList())).toList());
    }
}
