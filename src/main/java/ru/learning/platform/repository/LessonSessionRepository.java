package ru.learning.platform.repository;

import ru.learning.platform.entity.LessonSession;
import org.springframework.data.jpa.repository.JpaRepository;

public interface LessonSessionRepository extends JpaRepository<LessonSession, Long> {
}
