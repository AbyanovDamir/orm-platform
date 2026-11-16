package ru.learning.platform.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.learning.platform.service.LearningService;

import java.util.List;

import static ru.learning.platform.service.PlatformData.*;

@RestController
@RequestMapping("/api")
public class LearningController {
    private final LearningService service;

    public LearningController(LearningService service) {
        this.service = service;
    }

    @PostMapping("/courses/{id}/enrollments/{studentId}")
    @ResponseStatus(HttpStatus.CREATED)
    public EnrollmentView enroll(@PathVariable @Positive Long id, @PathVariable @Positive Long studentId) {
        return service.enroll(id, studentId);
    }

    @PutMapping("/courses/{id}/enrollments/{studentId}/completion")
    public EnrollmentView complete(@PathVariable @Positive Long id, @PathVariable @Positive Long studentId, @RequestParam @Positive Long teacherId) {
        return service.complete(id, studentId, teacherId);
    }

    @DeleteMapping("/courses/{id}/enrollments/{studentId}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void unenroll(@PathVariable @Positive Long id, @PathVariable @Positive Long studentId) {
        service.unenroll(id, studentId);
    }

    @GetMapping("/courses/{id}/enrollments")
    public List<EnrollmentView> courseEnrollments(@PathVariable @Positive Long id) {
        return service.courseEnrollments(id);
    }

    @GetMapping("/users/{id}/enrollments")
    public List<EnrollmentView> studentEnrollments(@PathVariable @Positive Long id) {
        return service.studentEnrollments(id);
    }

    @PostMapping("/assignments/{id}/submissions")
    @ResponseStatus(HttpStatus.CREATED)
    public SubmissionView submit(@PathVariable @Positive Long id, @Valid @RequestBody SubmissionInput input) {
        return service.submit(id, input);
    }

    @PutMapping("/submissions/{id}/grade")
    public SubmissionView grade(@PathVariable @Positive Long id, @Valid @RequestBody GradeInput input) {
        return service.grade(id, input);
    }

    @GetMapping("/assignments/{id}/submissions")
    public List<SubmissionView> assignmentSubmissions(@PathVariable @Positive Long id) {
        return service.assignmentSubmissions(id);
    }

    @GetMapping("/users/{id}/submissions")
    public List<SubmissionView> studentSubmissions(@PathVariable @Positive Long id) {
        return service.studentSubmissions(id);
    }

    @GetMapping("/courses/{id}/reviews")
    public List<ReviewView> reviews(@PathVariable @Positive Long id) {
        return service.reviews(id);
    }

    @PostMapping("/courses/{id}/reviews")
    @ResponseStatus(HttpStatus.CREATED)
    public ReviewView review(@PathVariable @Positive Long id, @Valid @RequestBody ReviewInput input) {
        return service.review(id, input);
    }
}
