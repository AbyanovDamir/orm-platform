package ru.learning.platform.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.learning.platform.service.CatalogService;

import java.util.List;

import static ru.learning.platform.service.PlatformData.*;

@RestController
@RequestMapping("/api")
public class CatalogController {
    private final CatalogService service;

    public CatalogController(CatalogService service) {
        this.service = service;
    }

    @GetMapping("/courses")
    public List<CourseView> courses() {
        return service.courses();
    }

    @GetMapping("/courses/{id}")
    public CourseDetail course(@PathVariable @Positive Long id) {
        return service.course(id);
    }

    @PostMapping("/courses")
    @ResponseStatus(HttpStatus.CREATED)
    public CourseView createCourse(@Valid @RequestBody CourseInput input) {
        return service.createCourse(input);
    }

    @PutMapping("/courses/{id}")
    public CourseView updateCourse(@PathVariable @Positive Long id, @Valid @RequestBody CourseInput input) {
        return service.updateCourse(id, input);
    }

    @DeleteMapping("/courses/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteCourse(@PathVariable @Positive Long id) {
        service.deleteCourse(id);
    }

    @GetMapping("/modules/{id}")
    public ModuleView module(@PathVariable @Positive Long id) {
        return service.module(id);
    }

    @PostMapping("/courses/{id}/modules")
    @ResponseStatus(HttpStatus.CREATED)
    public ModuleView createModule(@PathVariable @Positive Long id, @Valid @RequestBody ModuleInput input) {
        return service.createModule(id, input);
    }

    @PutMapping("/modules/{id}")
    public ModuleView updateModule(@PathVariable @Positive Long id, @Valid @RequestBody ModuleInput input) {
        return service.updateModule(id, input);
    }

    @DeleteMapping("/modules/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteModule(@PathVariable @Positive Long id) {
        service.deleteModule(id);
    }

    @GetMapping("/lessons/{id}")
    public LessonView lesson(@PathVariable @Positive Long id) {
        return service.lesson(id);
    }

    @PostMapping("/modules/{id}/lessons")
    @ResponseStatus(HttpStatus.CREATED)
    public LessonView createLesson(@PathVariable @Positive Long id, @Valid @RequestBody LessonInput input) {
        return service.createLesson(id, input);
    }

    @PutMapping("/lessons/{id}")
    public LessonView updateLesson(@PathVariable @Positive Long id, @Valid @RequestBody LessonInput input) {
        return service.updateLesson(id, input);
    }

    @DeleteMapping("/lessons/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteLesson(@PathVariable @Positive Long id) {
        service.deleteLesson(id);
    }

    @GetMapping("/sessions/{id}")
    public SessionView session(@PathVariable @Positive Long id) {
        return service.session(id);
    }

    @PostMapping("/lessons/{id}/sessions")
    @ResponseStatus(HttpStatus.CREATED)
    public SessionView createSession(@PathVariable @Positive Long id, @Valid @RequestBody SessionInput input) {
        return service.createSession(id, input);
    }

    @PutMapping("/sessions/{id}")
    public SessionView updateSession(@PathVariable @Positive Long id, @Valid @RequestBody SessionInput input) {
        return service.updateSession(id, input);
    }

    @DeleteMapping("/sessions/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteSession(@PathVariable @Positive Long id) {
        service.deleteSession(id);
    }

    @GetMapping("/assignments/{id}")
    public AssignmentView assignment(@PathVariable @Positive Long id) {
        return service.assignment(id);
    }

    @PostMapping("/lessons/{id}/assignments")
    @ResponseStatus(HttpStatus.CREATED)
    public AssignmentView createAssignment(@PathVariable @Positive Long id, @Valid @RequestBody AssignmentInput input) {
        return service.createAssignment(id, input);
    }

    @PutMapping("/assignments/{id}")
    public AssignmentView updateAssignment(@PathVariable @Positive Long id, @Valid @RequestBody AssignmentInput input) {
        return service.updateAssignment(id, input);
    }

    @DeleteMapping("/assignments/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteAssignment(@PathVariable @Positive Long id) {
        service.deleteAssignment(id);
    }
}
