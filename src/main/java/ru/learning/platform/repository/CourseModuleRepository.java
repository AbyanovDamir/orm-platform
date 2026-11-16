package ru.learning.platform.repository;

import ru.learning.platform.entity.CourseModule;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CourseModuleRepository extends JpaRepository<CourseModule, Long> {
}
