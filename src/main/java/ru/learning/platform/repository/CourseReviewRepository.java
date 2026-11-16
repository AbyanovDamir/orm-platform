package ru.learning.platform.repository;

import ru.learning.platform.entity.CourseReview;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface CourseReviewRepository extends JpaRepository<CourseReview, Long> {
    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
    List<CourseReview> findByCourseIdOrderByIdAsc(Long courseId);
}
