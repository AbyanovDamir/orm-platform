package ru.learning.platform.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.learning.platform.entity.*;
import ru.learning.platform.repository.*;

import java.time.Instant;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import static ru.learning.platform.service.PlatformData.*;
import static ru.learning.platform.service.PlatformException.*;

@Service
@Transactional(readOnly = true)
public class QuizService {
    private final CourseModuleRepository modules;
    private final QuizRepository quizzes;
    private final QuizSubmissionRepository results;
    private final LearningAccess access;

    public QuizService(CourseModuleRepository modules, QuizRepository quizzes, QuizSubmissionRepository results,
                       LearningAccess access) {
        this.modules = modules;
        this.quizzes = quizzes;
        this.results = results;
        this.access = access;
    }

    @Transactional
    public QuizView createQuiz(Long moduleId, QuizInput input) {
        CourseModule module = require(modules.findById(moduleId), "Модуль", moduleId);
        check(!quizzes.existsByModuleId(moduleId), Kind.CONFLICT, "У модуля уже есть тест");
        Quiz quiz = new Quiz(module, input.title(), input.timeLimit());
        for (QuestionInput item : input.questions()) {
            long correct = item.options().stream().filter(OptionInput::correct).count();
            boolean valid = item.type() == QuestionType.SINGLE_CHOICE ? correct == 1 : correct > 0;
            check(valid, Kind.BAD_INPUT, "Неверное число правильных вариантов в вопросе: " + item.text());
            Question question = new Question(quiz, item.text(), item.type());
            quiz.getQuestions().add(question);
            for (OptionInput option : item.options()) {
                question.getOptions().add(new AnswerOption(question, option.text(), option.correct()));
            }
        }
        return Views.quiz(quizzes.save(quiz));
    }

    public QuizView quiz(Long id) {
        return Views.quiz(findQuiz(id));
    }

    @Transactional
    public QuizView updateQuiz(Long id, QuizMetadata input) {
        Quiz quiz = findQuiz(id);
        quiz.setTitle(input.title());
        quiz.setTimeLimit(input.timeLimit());
        return Views.quiz(quiz);
    }

    @Transactional
    public void deleteQuiz(Long id) {
        quizzes.delete(findQuiz(id));
    }

    @Transactional
    public AttemptView takeQuiz(Long quizId, AttemptInput input) {
        Quiz quiz = findQuiz(quizId);
        User student = access.student(input.studentId());
        access.requireActive(quiz.getModule().getCourse().getId(), student.getId());
        List<QuizScoring.QuestionKey> keys = quiz.getQuestions().stream().map(QuizService::answerKey).toList();
        check(!keys.isEmpty(), Kind.CONFLICT, "Тест не содержит вопросов");
        int score = QuizScoring.score(keys, input.answers());
        return Views.attempt(results.save(new QuizSubmission(quiz, student, score, keys.size(), Instant.now())));
    }

    public List<AttemptView> quizAttempts(Long id) {
        require(quizzes.findById(id), "Тест", id);
        return results.findByQuizIdOrderByIdDesc(id).stream().map(Views::attempt).toList();
    }

    public List<AttemptView> studentAttempts(Long id) {
        access.student(id);
        return results.findByStudentIdOrderByIdDesc(id).stream().map(Views::attempt).toList();
    }

    private Quiz findQuiz(Long id) {
        return require(quizzes.findWithQuestions(id), "Тест", id);
    }

    private static QuizScoring.QuestionKey answerKey(Question question) {
        Set<Long> all = question.getOptions().stream().map(AnswerOption::getId).collect(Collectors.toSet());
        Set<Long> correct = question.getOptions().stream().filter(AnswerOption::isCorrect)
                .map(AnswerOption::getId).collect(Collectors.toSet());
        return new QuizScoring.QuestionKey(question.getId(), question.getType(), all, correct);
    }
}
