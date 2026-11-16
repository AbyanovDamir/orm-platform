package ru.learning.platform.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.learning.platform.entity.*;
import ru.learning.platform.repository.*;

import java.util.List;

import static ru.learning.platform.service.PlatformData.*;
import static ru.learning.platform.service.PlatformException.*;

@Service
@Transactional(readOnly = true)
public class CatalogService {
    private final CourseRepository courses;
    private final CategoryRepository categories;
    private final UserRepository users;
    private final TagRepository tags;
    private final SubmissionRepository submissions;
    private final CourseModuleRepository modules;
    private final LessonRepository lessons;
    private final LessonSessionRepository sessions;
    private final AssignmentRepository assignments;

    public CatalogService(CourseRepository courses, CategoryRepository categories, UserRepository users,
                          TagRepository tags, CourseModuleRepository modules, LessonRepository lessons,
                          LessonSessionRepository sessions, AssignmentRepository assignments,
                          SubmissionRepository submissions) {
        this.courses = courses;
        this.categories = categories;
        this.users = users;
        this.tags = tags;
        this.modules = modules;
        this.lessons = lessons;
        this.sessions = sessions;
        this.assignments = assignments;
        this.submissions = submissions;
    }

    public List<CourseView> courses() {
        return courses.findAllByOrderByIdAsc().stream().map(Views::course).toList();
    }

    public CourseDetail course(Long id) {
        return Views.courseDetail(require(courses.findWithModules(id), "Курс", id));
    }

    @Transactional
    public CourseView createCourse(CourseInput input) {
        Category category = require(categories.findById(input.categoryId()), "Категория", input.categoryId());
        Course course = new Course(input.title(), input.description(), category, teacher(input.teacherId()),
                input.duration(), input.startDate());
        course.getTags().addAll(courseTags(input));
        return Views.course(courses.save(course));
    }

    @Transactional
    public CourseView updateCourse(Long id, CourseInput input) {
        Course course = require(courses.findById(id), "Курс", id);
        course.setCategory(require(categories.findById(input.categoryId()), "Категория", input.categoryId()));
        course.setTeacher(teacher(input.teacherId()));
        course.setTitle(input.title());
        course.setDescription(input.description());
        course.setDuration(input.duration());
        course.setStartDate(input.startDate());
        List<Tag> selected = courseTags(input);
        course.getTags().clear();
        course.getTags().addAll(selected);
        return Views.course(course);
    }

    @Transactional
    public void deleteCourse(Long id) {
        courses.delete(require(courses.findById(id), "Курс", id));
    }

    public ModuleView module(Long id) {
        return Views.module(require(modules.findById(id), "Модуль", id));
    }

    @Transactional
    public ModuleView createModule(Long parentId, ModuleInput input) {
        Course course = require(courses.findById(parentId), "Курс", parentId);
        return Views.module(modules.save(new CourseModule(course, input.title(), input.orderIndex(), input.description())));
    }

    @Transactional
    public ModuleView updateModule(Long id, ModuleInput input) {
        CourseModule entity = require(modules.findById(id), "Модуль", id);
        entity.setTitle(input.title());
        entity.setOrderIndex(input.orderIndex());
        entity.setDescription(input.description());
        return Views.module(entity);
    }

    @Transactional
    public void deleteModule(Long id) {
        modules.delete(require(modules.findById(id), "Модуль", id));
    }

    public LessonView lesson(Long id) {
        return Views.lesson(require(lessons.findById(id), "Урок", id));
    }

    @Transactional
    public LessonView createLesson(Long parentId, LessonInput input) {
        CourseModule module = require(modules.findById(parentId), "Модуль", parentId);
        return Views.lesson(lessons.save(new Lesson(module, input.title(), input.content(), input.videoUrl())));
    }

    @Transactional
    public LessonView updateLesson(Long id, LessonInput input) {
        Lesson entity = require(lessons.findById(id), "Урок", id);
        entity.setTitle(input.title());
        entity.setContent(input.content());
        entity.setVideoUrl(input.videoUrl());
        return Views.lesson(entity);
    }

    @Transactional
    public void deleteLesson(Long id) {
        lessons.delete(require(lessons.findById(id), "Урок", id));
    }

    public SessionView session(Long id) {
        return Views.session(require(sessions.findById(id), "Занятие", id));
    }

    @Transactional
    public SessionView createSession(Long parentId, SessionInput input) {
        validateSession(input);
        Lesson lesson = require(lessons.findById(parentId), "Урок", parentId);
        return Views.session(sessions.save(new LessonSession(lesson, input.startsAt(), input.endsAt(), input.location())));
    }

    @Transactional
    public SessionView updateSession(Long id, SessionInput input) {
        validateSession(input);
        LessonSession entity = require(sessions.findById(id), "Занятие", id);
        entity.setStartsAt(input.startsAt());
        entity.setEndsAt(input.endsAt());
        entity.setLocation(input.location());
        return Views.session(entity);
    }

    @Transactional
    public void deleteSession(Long id) {
        sessions.delete(require(sessions.findById(id), "Занятие", id));
    }

    public AssignmentView assignment(Long id) {
        return Views.assignment(require(assignments.findById(id), "Задание", id));
    }

    @Transactional
    public AssignmentView createAssignment(Long parentId, AssignmentInput input) {
        Lesson lesson = require(lessons.findById(parentId), "Урок", parentId);
        return Views.assignment(assignments.save(new Assignment(lesson, input.title(), input.description(), input.dueDate(), input.maxScore())));
    }

    @Transactional
    public AssignmentView updateAssignment(Long id, AssignmentInput input) {
        Assignment entity = require(assignments.lockById(id), "Задание", id);
        check(!submissions.existsByAssignmentIdAndScoreGreaterThan(id, input.maxScore()),
                Kind.CONFLICT, "Максимальный балл ниже ранее выставленной оценки");
        entity.setTitle(input.title());
        entity.setDescription(input.description());
        entity.setDueDate(input.dueDate());
        entity.setMaxScore(input.maxScore());
        return Views.assignment(entity);
    }

    @Transactional
    public void deleteAssignment(Long id) {
        assignments.delete(require(assignments.findById(id), "Задание", id));
    }

    private User teacher(Long id) {
        User teacher = require(users.findById(id), "Пользователь", id);
        check(teacher.getRole() == Role.TEACHER, Kind.BAD_INPUT, "Курс должен вести преподаватель");
        return teacher;
    }

    private List<Tag> courseTags(CourseInput input) {
        List<Tag> selected = tags.findAllById(input.tagIds());
        check(selected.size() == input.tagIds().size(), Kind.BAD_INPUT, "Один или несколько тегов отсутствуют");
        return selected;
    }

    private static void validateSession(SessionInput input) {
        check(input.endsAt().isAfter(input.startsAt()), Kind.BAD_INPUT,
                "Занятие должно заканчиваться позже начала");
    }
}
