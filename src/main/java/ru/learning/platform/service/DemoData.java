package ru.learning.platform.service;

import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import ru.learning.platform.entity.QuestionType;
import ru.learning.platform.entity.Role;
import ru.learning.platform.repository.UserRepository;

import java.time.Instant;
import java.time.LocalDate;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static ru.learning.platform.service.PlatformData.*;

@Component
@ConditionalOnProperty(name = "platform.demo", havingValue = "true")
class DemoData implements ApplicationRunner {
    private final DirectoryService directory;
    private final CatalogService catalog;
    private final LearningService learning;
    private final QuizService quizzes;
    private final UserRepository users;

    DemoData(DirectoryService directory, CatalogService catalog, LearningService learning,
             QuizService quizzes, UserRepository users) {
        this.directory = directory;
        this.catalog = catalog;
        this.learning = learning;
        this.quizzes = quizzes;
        this.users = users;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        if (users.findByEmail("teacher@orm.example").isPresent()) {
            return;
        }
        LocalDate today = LocalDate.now(ZoneOffset.UTC);
        UserView teacher = directory.createUser(new UserInput("Мария Волкова", "teacher@orm.example", Role.TEACHER));
        UserView student = directory.createUser(new UserInput("Алексей Орлов", "student@orm.example", Role.STUDENT));
        UserView guest = directory.createUser(new UserInput("Анна Белова", "anna@orm.example", Role.STUDENT));
        directory.saveProfile(teacher.id(), new ProfileInput("Ведёт практикум по Java и базам данных.", null));
        NamedView category = directory.createCategory(new CategoryInput("Java и базы данных"));
        NamedView tag = directory.createTag(new TagInput("Hibernate"));
        CourseView course = catalog.createCourse(new CourseInput("Основы Hibernate",
                "Спроектируйте модель данных, настройте связи JPA и разберите ленивую загрузку.",
                category.id(), teacher.id(), "2 недели", today, Set.of(tag.id())));
        ModuleView mapping = catalog.createModule(course.id(), new ModuleInput("Модель данных", 1, "JPA и связи"));
        LessonView lesson = catalog.createLesson(mapping.id(), new LessonInput("Сущности и связи",
                "Определите владельца связи и проверьте, какой столбец хранит внешний ключ.", null));
        AssignmentView homework = catalog.createAssignment(lesson.id(), new AssignmentInput("Связь курса и модуля",
                "Опишите обе стороны связи и укажите, где расположен внешний ключ.", today.plusDays(60), 10));
        Instant starts = Instant.now().truncatedTo(ChronoUnit.HOURS).plus(1, ChronoUnit.DAYS);
        catalog.createSession(lesson.id(), new SessionInput(starts, starts.plus(90, ChronoUnit.MINUTES), "Онлайн"));
        ModuleView fetching = catalog.createModule(course.id(), new ModuleInput("Ленивая загрузка", 2, "Сессия и запросы"));
        catalog.createLesson(fetching.id(), new LessonInput("Границы сессии",
                "Сравните findById, EntityGraph и JOIN FETCH при чтении модулей курса.", null));
        QuizView quiz = quizzes.createQuiz(fetching.id(), new QuizInput("Проверка знаний JPA", 15, List.of(
                new QuestionInput("Какая аннотация обозначает первичный ключ?", QuestionType.SINGLE_CHOICE,
                        List.of(new OptionInput("@Id", true), new OptionInput("@Table", false))),
                new QuestionInput("Какие запросы могут заранее загрузить модули курса?", QuestionType.MULTIPLE_CHOICE,
                        List.of(new OptionInput("Запрос с EntityGraph", true),
                                new OptionInput("JPQL с JOIN FETCH", true), new OptionInput("Обычный findById", false))))));
        learning.enroll(course.id(), student.id());
        SubmissionView submission = learning.submit(homework.id(), new SubmissionInput(student.id(),
                "Внешний ключ course_id находится в таблице modules. Владелец связи — модуль."));
        learning.grade(submission.id(), new GradeInput(teacher.id(), 9, "Добавьте пояснение для mappedBy."));
        QuestionView single = quiz.questions().get(0);
        QuestionView multiple = quiz.questions().get(1);
        quizzes.takeQuiz(quiz.id(), new AttemptInput(student.id(), Map.of(
                single.id(), Set.of(single.options().get(0).id()),
                multiple.id(), Set.of(multiple.options().get(0).id(), multiple.options().get(1).id()))));
        learning.enroll(course.id(), guest.id());
        learning.complete(course.id(), guest.id(), teacher.id());
        learning.review(course.id(), new ReviewInput(guest.id(), 5, "Примеры помогли понять границы сессии."));
        learning.unenroll(course.id(), guest.id());
    }
}
