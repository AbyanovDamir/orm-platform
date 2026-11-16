package ru.learning.platform.web;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Positive;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import ru.learning.platform.service.QuizService;

import java.util.List;

import static ru.learning.platform.service.PlatformData.*;

@RestController
@RequestMapping("/api")
public class QuizController {
    private final QuizService service;

    public QuizController(QuizService service) {
        this.service = service;
    }

    @GetMapping("/quizzes/{id}")
    public QuizView quiz(@PathVariable @Positive Long id) {
        return service.quiz(id);
    }

    @PostMapping("/modules/{id}/quiz")
    @ResponseStatus(HttpStatus.CREATED)
    public QuizView createQuiz(@PathVariable @Positive Long id, @Valid @RequestBody QuizInput input) {
        return service.createQuiz(id, input);
    }

    @PutMapping("/quizzes/{id}")
    public QuizView updateQuiz(@PathVariable @Positive Long id, @Valid @RequestBody QuizMetadata input) {
        return service.updateQuiz(id, input);
    }

    @DeleteMapping("/quizzes/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteQuiz(@PathVariable @Positive Long id) {
        service.deleteQuiz(id);
    }

    @PostMapping("/quizzes/{id}/attempts")
    @ResponseStatus(HttpStatus.CREATED)
    public AttemptView takeQuiz(@PathVariable @Positive Long id, @Valid @RequestBody AttemptInput input) {
        return service.takeQuiz(id, input);
    }

    @GetMapping("/quizzes/{id}/attempts")
    public List<AttemptView> quizAttempts(@PathVariable @Positive Long id) {
        return service.quizAttempts(id);
    }

    @GetMapping("/users/{id}/quiz-attempts")
    public List<AttemptView> studentAttempts(@PathVariable @Positive Long id) {
        return service.studentAttempts(id);
    }
}
