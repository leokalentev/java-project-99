package hexlet.code.repository;

import hexlet.code.model.TaskStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface TaskStatusRepository extends JpaRepository<TaskStatus, Long> {
    boolean existsByName(String name);
    boolean existsBySlug(String slug);
    Optional<TaskStatus> findBySlug(String slug);
}
