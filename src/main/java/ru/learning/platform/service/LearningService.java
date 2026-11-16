package ru.learning.platform.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.learning.platform.entity.*;
import ru.learning.platform.repository.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;

import static ru.learning.platform.service.PlatformData.*;
import static ru.learning.platform.service.PlatformException.*;

@Service
@Transactional(readOnly = true)
public class LearningService {
    private final CourseRepository courses;
    private final EnrollmentRepository enrollments;
    private final AssignmentRepository assignments;
    private final SubmissionRepository submissions;
    private final CourseReviewRepository reviews;
    private final LearningAccess access;

    public LearningService(CourseRepository courses, EnrollmentRepository enrollments, AssignmentRepository assignments,
                           SubmissionRepository submissions, CourseReviewRepository reviews, LearningAccess access) {
        this.courses = courses;
        this.enrollments = enrollments;
        this.assignments = assignments;
        this.submissions = submissions;
        this.reviews = reviews;
        this.access = access;
    }

    @Transactional
    public EnrollmentView enroll(Long courseId, Long studentId) {
        Course course = course(courseId);
        User student = access.student(studentId);
        check(enrollments.findByCourseIdAndStudentId(courseId, studentId).isEmpty(), Kind.CONFLICT,
                "Студент уже записан на курс");
        return Views.enrollment(enrollments.save(
                new Enrollment(student, course, LocalDate.now(ZoneOffset.UTC), EnrollmentStatus.ACTIVE)));
    }

    @Transactional
    public EnrollmentView complete(Long courseId, Long studentId, Long teacherId) {
        check(course(courseId).getTeacher().getId().equals(teacherId), Kind.FORBIDDEN,
                "Завершить обучение может преподаватель курса");
        Enrollment enrollment = enrollment(courseId, studentId);
        enrollment.setStatus(EnrollmentStatus.COMPLETED);
        return Views.enrollment(enrollment);
    }

    @Transactional
    public void unenroll(Long courseId, Long studentId) {
        enrollments.delete(enrollment(courseId, studentId));
    }

    public List<EnrollmentView> courseEnrollments(Long courseId) {
        course(courseId);
        return enrollments.findByCourseIdOrderByIdAsc(courseId).stream().map(Views::enrollment).toList();
    }

    public List<EnrollmentView> studentEnrollments(Long studentId) {
        access.student(studentId);
        return enrollments.findByStudentIdOrderByIdAsc(studentId).stream().map(Views::enrollment).toList();
    }

    @Transactional
    public SubmissionView submit(Long assignmentId, SubmissionInput input) {
        Assignment assignment = require(assignments.lockById(assignmentId), "Задание", assignmentId);
        User student = access.student(input.studentId());
        access.requireActive(assignment.getLesson().getModule().getCourse().getId(), student.getId());
        check(!submissions.existsByAssignmentIdAndStudentId(assignmentId, student.getId()), Kind.CONFLICT,
                "Работа уже сдана");
        check(!LocalDate.now(ZoneOffset.UTC).isAfter(assignment.getDueDate()), Kind.CONFLICT,
                "Срок сдачи задания истёк");
        Submission submission = new Submission(assignment, student, Instant.now(), input.content(), null,
                null, SubmissionStatus.SUBMITTED);
        return Views.submission(submissions.save(submission));
    }

    @Transactional
    public SubmissionView grade(Long id, GradeInput input) {
        Submission submission = require(submissions.findById(id), "Решение", id);
        Long assignmentId = submission.getAssignment().getId();
        Assignment assignment = require(assignments.lockById(assignmentId), "Задание", assignmentId);
        Long teacherId = assignment.getLesson().getModule().getCourse().getTeacher().getId();
        check(teacherId.equals(input.teacherId()), Kind.FORBIDDEN, "Оценку выставляет преподаватель курса");
        check(input.score() >= 0 && input.score() <= assignment.getMaxScore(), Kind.BAD_INPUT,
                "Оценка должна быть от 0 до " + assignment.getMaxScore());
        submission.setScore(input.score());
        submission.setFeedback(input.feedback());
        submission.setStatus(SubmissionStatus.GRADED);
        return Views.submission(submission);
    }

    public List<SubmissionView> assignmentSubmissions(Long assignmentId) {
        require(assignments.findById(assignmentId), "Задание", assignmentId);
        return submissions.findByAssignmentIdOrderByIdAsc(assignmentId).stream().map(Views::submission).toList();
    }

    public List<SubmissionView> studentSubmissions(Long studentId) {
        access.student(studentId);
        return submissions.findByStudentIdOrderByIdAsc(studentId).stream().map(Views::submission).toList();
    }

    @Transactional
    public ReviewView review(Long courseId, ReviewInput input) {
        Course course = course(courseId);
        User student = access.student(input.studentId());
        check(enrollment(courseId, student.getId()).getStatus() == EnrollmentStatus.COMPLETED, Kind.FORBIDDEN,
                "Отзыв доступен после завершения курса");
        check(!reviews.existsByCourseIdAndStudentId(courseId, student.getId()), Kind.CONFLICT, "Отзыв уже оставлен");
        return Views.review(reviews.save(new CourseReview(course, student, input.rating(), input.comment(), Instant.now())));
    }

    public List<ReviewView> reviews(Long courseId) {
        course(courseId);
        return reviews.findByCourseIdOrderByIdAsc(courseId).stream().map(Views::review).toList();
    }

    private Course course(Long id) {
        return require(courses.findById(id), "Курс", id);
    }

    private Enrollment enrollment(Long courseId, Long studentId) {
        return enrollments.findByCourseIdAndStudentId(courseId, studentId)
                .orElseThrow(() -> new PlatformException(Kind.NOT_FOUND, "Запись на курс отсутствует"));
    }
}
