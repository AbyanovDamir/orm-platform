package ru.learning.platform.repository;

import ru.learning.platform.entity.Submission;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface SubmissionRepository extends JpaRepository<Submission, Long> {
    boolean existsByAssignmentIdAndStudentId(Long assignmentId, Long studentId);
    boolean existsByAssignmentIdAndScoreGreaterThan(Long assignmentId, int score);
    List<Submission> findByAssignmentIdOrderByIdAsc(Long assignmentId);
    List<Submission> findByStudentIdOrderByIdAsc(Long studentId);
}
