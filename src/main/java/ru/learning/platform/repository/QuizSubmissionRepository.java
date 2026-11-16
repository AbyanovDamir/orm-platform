package ru.learning.platform.repository;

import ru.learning.platform.entity.QuizSubmission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface QuizSubmissionRepository extends JpaRepository<QuizSubmission, Long> {
    List<QuizSubmission> findByQuizIdOrderByIdDesc(Long quizId);
    List<QuizSubmission> findByStudentIdOrderByIdDesc(Long studentId);
}
