package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.CourseTaskAllowedFormat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CourseTaskAllowedFormatRepository extends JpaRepository<CourseTaskAllowedFormat, Long> {
    List<CourseTaskAllowedFormat> findByTaskId(Long taskId);
    List<CourseTaskAllowedFormat> findByTaskIdIn(List<Long> taskIds);

    @Modifying
    @Query("DELETE FROM CourseTaskAllowedFormat f WHERE f.taskId = :taskId")
    void deleteByTaskId(@Param("taskId") Long taskId);
}
