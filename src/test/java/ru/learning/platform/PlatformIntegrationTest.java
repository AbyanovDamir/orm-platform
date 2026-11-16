package ru.learning.platform;

import org.hibernate.Hibernate;
import org.hibernate.LazyInitializationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.support.TransactionTemplate;
import ru.learning.platform.entity.*;
import ru.learning.platform.repository.*;
import ru.learning.platform.service.*;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import static ru.learning.platform.service.PlatformData.*;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
class PlatformIntegrationTest {
    @Autowired DirectoryService directory;
    @Autowired CatalogService catalog;
    @Autowired LearningService learning;
    @Autowired QuizService quizzes;
    @Autowired CourseRepository courses;
    @Autowired TagRepository tags;
    @Autowired LessonRepository lessons;
    @Autowired AssignmentRepository assignments;
    @Autowired EnrollmentRepository enrollments;
    @Autowired SubmissionRepository submissions;
    @Autowired QuizSubmissionRepository attempts;
    @Autowired JdbcTemplate jdbc;
    @Autowired TransactionTemplate transactions;
    @Autowired MockMvc mvc;

    private UserView teacher;
    private UserView student;
    private NamedView category;
    private NamedView tag;
    private CourseView course;
    private ModuleView module;
    private LessonView lesson;
    private AssignmentView assignment;
    private LocalDate today;

    @BeforeEach
    void fixture() {
        String suffix = UUID.randomUUID().toString();
        today = LocalDate.now(ZoneOffset.UTC);
        teacher = directory.createUser(new UserInput("Преподаватель", "t-" + suffix + "@example.org", Role.TEACHER));
        student = directory.createUser(new UserInput("Студент", "s-" + suffix + "@example.org", Role.STUDENT));
        category = directory.createCategory(new CategoryInput("Категория " + suffix));
        tag = directory.createTag(new TagInput("Тег " + suffix));
        course = catalog.createCourse(courseInput("JPA", teacher.id()));
        module = catalog.createModule(course.id(), new ModuleInput("Связи", 1, null));
        lesson = catalog.createLesson(module.id(), new LessonInput("Практика", "Пример", null));
        assignment = catalog.createAssignment(lesson.id(), assignmentInput(today.plusDays(1), 10));
    }

    @Test
    void createsSeventeenTablesAndReadsNestedContent() {
        List<String> expected = List.of("users", "profiles", "categories", "tags", "courses", "course_tag", "modules",
                "lessons", "lesson_sessions", "enrollments", "assignments", "submissions", "quizzes",
                "questions", "answer_options", "quiz_submissions", "course_reviews");
        assertThat(jdbc.queryForList("select lower(table_name) from information_schema.tables "
                + "where lower(table_schema) = 'public'", String.class)).containsAll(expected);
        Instant start = Instant.parse("2035-01-02T10:00:00Z");
        SessionView session = catalog.createSession(lesson.id(), new SessionInput(start, start.plusSeconds(3600), "Онлайн"));
        CourseDetail detail = catalog.course(course.id());
        assertThat(detail.course().tags()).containsExactly(tag);
        assertThat(detail.modules().get(0).lessons().get(0).assignments()).containsExactly(assignment);
        assertThat(detail.modules().get(0).lessons().get(0).sessions()).containsExactly(session);
    }

    @Test
    void updatesCourseAndLesson() {
        catalog.updateCourse(course.id(), courseInput("Hibernate", teacher.id()));
        catalog.updateLesson(lesson.id(), new LessonInput("Новое название", "Обновлённый текст", null));
        assertThat(catalog.course(course.id()).course().title()).isEqualTo("Hibernate");
        assertThat(catalog.lesson(lesson.id()).content()).isEqualTo("Обновлённый текст");
    }

    @Test
    void createsUpdatesAndDeletesProfile() {
        ProfileView first = directory.saveProfile(student.id(), new ProfileInput("Первый текст", null));
        ProfileView second = directory.saveProfile(student.id(), new ProfileInput("Второй текст", null));
        assertThat(second.id()).isEqualTo(first.id());
        assertThat(directory.profile(student.id()).bio()).isEqualTo("Второй текст");
        directory.deleteProfile(student.id());
        assertThatThrownBy(() -> directory.profile(student.id())).isInstanceOf(PlatformException.class);
    }

    @Test
    void cascadesCourseDeletionWithoutDeletingTag() {
        learning.enroll(course.id(), student.id());
        SubmissionView work = learning.submit(assignment.id(), new SubmissionInput(student.id(), "Ответ"));
        QuizView quiz = createQuiz();
        AttemptView attempt = quizzes.takeQuiz(quiz.id(), new AttemptInput(student.id(), correct(quiz)));
        catalog.deleteCourse(course.id());
        assertThat(courses.existsById(course.id())).isFalse();
        assertThat(lessons.existsById(lesson.id())).isFalse();
        assertThat(assignments.existsById(assignment.id())).isFalse();
        assertThat(submissions.existsById(work.id())).isFalse();
        assertThat(attempts.existsById(attempt.id())).isFalse();
        assertThat(enrollments.findByStudentIdOrderByIdAsc(student.id())).isEmpty();
        assertThat(tags.existsById(tag.id())).isTrue();
    }

    @Test
    void rejectsNonTeacherInCourse() {
        assertThatThrownBy(() -> catalog.createCourse(courseInput("Недопустимый курс", student.id())))
                .isInstanceOf(PlatformException.class);
    }

    @Test
    void enrollsListsAndUnenrollsStudent() {
        EnrollmentView entry = learning.enroll(course.id(), student.id());
        assertThat(learning.courseEnrollments(course.id())).containsExactly(entry);
        assertThat(learning.studentEnrollments(student.id())).containsExactly(entry);
        assertThatThrownBy(() -> learning.enroll(course.id(), student.id())).isInstanceOf(PlatformException.class);
        learning.unenroll(course.id(), student.id());
        assertThat(learning.courseEnrollments(course.id())).isEmpty();
    }

    @Test
    void databaseEnforcesEnrollmentUniqueness() {
        learning.enroll(course.id(), student.id());
        assertThatThrownBy(() -> jdbc.update(
                "insert into enrollments (user_id, course_id, enroll_date, status) values (?, ?, ?, ?)",
                student.id(), course.id(), today, "ACTIVE")).isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void submitsOnceAndPersistsGrade() {
        learning.enroll(course.id(), student.id());
        SubmissionInput input = new SubmissionInput(student.id(), "Ответ");
        SubmissionView work = learning.submit(assignment.id(), input);
        assertThat(work.score()).isNull();
        assertThatThrownBy(() -> learning.submit(assignment.id(), input)).isInstanceOf(PlatformException.class);
        assertThatThrownBy(() -> learning.grade(work.id(), new GradeInput(student.id(), 8, null)))
                .isInstanceOf(PlatformException.class);
        assertThatThrownBy(() -> learning.grade(work.id(), new GradeInput(teacher.id(), 11, null)))
                .isInstanceOf(PlatformException.class);
        SubmissionView grade = learning.grade(work.id(), new GradeInput(teacher.id(), 8, "Хорошо"));
        assertThat(grade.status()).isEqualTo(SubmissionStatus.GRADED);
        assertThat(learning.assignmentSubmissions(assignment.id()).get(0).score()).isEqualTo(8);
        assertThat(learning.studentSubmissions(student.id()).get(0).feedback()).isEqualTo("Хорошо");
        assertThatThrownBy(() -> catalog.updateAssignment(assignment.id(), assignmentInput(today, 7)))
                .isInstanceOf(PlatformException.class);
    }

    @Test
    void rejectsSubmissionWithoutEnrollmentAndAfterDeadline() {
        SubmissionInput input = new SubmissionInput(student.id(), "Ответ");
        assertThatThrownBy(() -> learning.submit(assignment.id(), input)).isInstanceOf(PlatformException.class);
        learning.enroll(course.id(), student.id());
        catalog.updateAssignment(assignment.id(), assignmentInput(today.minusDays(1), 10));
        assertThatThrownBy(() -> learning.submit(assignment.id(), input)).isInstanceOf(PlatformException.class);
        assertThat(learning.studentSubmissions(student.id())).isEmpty();
    }

    @Test
    void scoresQuizAndPersistsMultipleAttempts() {
        learning.enroll(course.id(), student.id());
        QuizView quiz = createQuiz();
        AttemptView first = quizzes.takeQuiz(quiz.id(), new AttemptInput(student.id(), correct(quiz)));
        QuestionView question = quiz.questions().get(0);
        AttemptView second = quizzes.takeQuiz(quiz.id(), new AttemptInput(student.id(),
                Map.of(question.id(), Set.of(question.options().get(1).id()))));
        assertThat(first.score()).isEqualTo(1);
        assertThat(first.maxScore()).isEqualTo(1);
        assertThat(second.score()).isZero();
        assertThat(quizzes.quizAttempts(quiz.id())).extracting(AttemptView::id).containsExactly(second.id(), first.id());
        assertThat(quizzes.studentAttempts(student.id())).hasSize(2);
    }

    @Test
    void refusesQuizWithInvalidAnswerKey() {
        QuizInput invalid = new QuizInput("Неверный ключ", null, List.of(new QuestionInput("Вопрос",
                QuestionType.SINGLE_CHOICE, List.of(new OptionInput("A", true), new OptionInput("B", true)))));
        assertThatThrownBy(() -> quizzes.createQuiz(module.id(), invalid)).isInstanceOf(PlatformException.class);
        assertThat(catalog.course(course.id()).modules().get(0).quizId()).isNull();
    }

    @Test
    void allowsReviewAfterCompletionOnly() {
        learning.enroll(course.id(), student.id());
        ReviewInput review = new ReviewInput(student.id(), 5, "Отзыв");
        assertThatThrownBy(() -> learning.review(course.id(), review)).isInstanceOf(PlatformException.class);
        learning.complete(course.id(), student.id(), teacher.id());
        learning.review(course.id(), review);
        assertThat(learning.reviews(course.id())).extracting(ReviewView::rating).containsExactly(5);
    }

    @Test
    void lazyModulesThrowAfterSessionCloses() {
        Course detached = courses.findById(course.id()).orElseThrow();
        assertThat(Hibernate.isInitialized(detached.getModules())).isFalse();
        assertThatThrownBy(() -> detached.getModules().size()).isInstanceOf(LazyInitializationException.class);
    }

    @Test
    void loadsModulesWithEntityGraphFetchJoinAndTransaction() {
        assertThat(courses.findWithModules(course.id()).orElseThrow().getModules()).hasSize(1);
        assertThat(courses.findWithFetchJoin(course.id()).orElseThrow().getModules()).hasSize(1);
        Integer count = transactions.execute(status -> courses.findById(course.id()).orElseThrow().getModules().size());
        assertThat(count).isEqualTo(1);
    }

    @Test
    void validatesRestAndDoesNotExposeCorrectAnswers() throws Exception {
        QuizView quiz = createQuiz();
        mvc.perform(get("/api/courses/{id}", course.id())).andExpect(status().isOk())
                .andExpect(jsonPath("$.modules[0].lessons[0].assignments[0].id").value(assignment.id()));
        mvc.perform(get("/api/courses/{id}", Long.MAX_VALUE)).andExpect(status().isNotFound());
        mvc.perform(get("/api/courses/0")).andExpect(status().isBadRequest());
        mvc.perform(post("/api/courses").contentType("application/json").content("{}"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors").isArray());
        mvc.perform(get("/api/quizzes/{id}", quiz.id())).andExpect(status().isOk())
                .andExpect(jsonPath("$.questions[0].options[0].correct").doesNotExist());
        mvc.perform(post("/api/courses/{id}/enrollments/{studentId}", course.id(), student.id()))
                .andExpect(status().isCreated());
        mvc.perform(post("/api/courses/{id}/enrollments/{studentId}", course.id(), student.id()))
                .andExpect(status().isConflict());
    }

    private CourseInput courseInput(String title, Long teacherId) {
        return new CourseInput(title, "Описание", category.id(), teacherId, null, today, Set.of(tag.id()));
    }

    private AssignmentInput assignmentInput(LocalDate dueDate, int maxScore) {
        return new AssignmentInput("ДЗ", "Описание", dueDate, maxScore);
    }

    private QuizView createQuiz() {
        return quizzes.createQuiz(module.id(), new QuizInput("Тест", 15, List.of(new QuestionInput("Ключ?",
                QuestionType.SINGLE_CHOICE, List.of(new OptionInput("@Id", true), new OptionInput("@Table", false))))));
    }

    private Map<Long, Set<Long>> correct(QuizView quiz) {
        QuestionView question = quiz.questions().get(0);
        return Map.of(question.id(), Set.of(question.options().get(0).id()));
    }
}
