package ru.learning.platform.repository;

import ru.learning.platform.entity.Course;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.EntityGraph;

public interface CourseRepository extends JpaRepository<Course, Long> {
    @EntityGraph(attributePaths = {"teacher", "category"})
    List<Course> findAllByOrderByIdAsc();

    @EntityGraph(attributePaths = {"teacher", "category", "modules"})
    @Query("select c from Course c where c.id = :id")
    Optional<Course> findWithModules(@Param("id") Long id);

    @Query("select distinct c from Course c left join fetch c.modules where c.id = :id")
    Optional<Course> findWithFetchJoin(@Param("id") Long id);
}
