package ru.learning.platform.repository;

import ru.learning.platform.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

public interface QuizRepository extends JpaRepository<Quiz, Long> {
    boolean existsByModuleId(Long moduleId);

    @EntityGraph(attributePaths = "questions")
    @Query("select q from Quiz q where q.id = :id")
    Optional<Quiz> findWithQuestions(@Param("id") Long id);
}
