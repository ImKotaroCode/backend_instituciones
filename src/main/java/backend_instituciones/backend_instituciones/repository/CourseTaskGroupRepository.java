package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.CourseTaskGroup;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseTaskGroupRepository extends JpaRepository<CourseTaskGroup, Long> {
    List<CourseTaskGroup> findByTaskId(Long taskId);
    List<CourseTaskGroup> findByTaskIdIn(List<Long> taskIds);

    @Modifying
    @Query("DELETE FROM CourseTaskGroup g WHERE g.taskId = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);
}
