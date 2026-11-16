package ru.learning.platform.repository;

import ru.learning.platform.entity.Assignment;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.data.jpa.repository.Lock;
import jakarta.persistence.LockModeType;

public interface AssignmentRepository extends JpaRepository<Assignment, Long> {
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select a from Assignment a where a.id = :id")
    Optional<Assignment> lockById(@Param("id") Long id);
}
