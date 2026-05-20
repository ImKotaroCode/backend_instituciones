package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.CourseTask;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseTaskRepository extends JpaRepository<CourseTask, Long> {
    List<CourseTask> findByCourseIdAndInstitutionIdOrderByCreatedAtDesc(Long courseId, Long institutionId);
    Optional<CourseTask> findByIdAndInstitutionId(Long id, Long institutionId);
    Optional<CourseTask> findByGeneratedCodeAndInstitutionId(String generatedCode, Long institutionId);
    boolean existsByGeneratedCode(String generatedCode);

    @org.springframework.data.jpa.repository.Query(
            "SELECT t FROM CourseTask t WHERE t.institutionId = :institutionId " +
            "AND t.courseId IN :courseIds AND t.status = 'PUBLICADO' " +
            "AND (t.visibleFrom IS NULL OR t.visibleFrom <= :now) ORDER BY t.dueAt ASC")
    List<CourseTask> findVisiblePublishedTasks(
            @org.springframework.data.repository.query.Param("institutionId") Long institutionId,
            @org.springframework.data.repository.query.Param("courseIds") java.util.List<Long> courseIds,
            @org.springframework.data.repository.query.Param("now") java.time.LocalDateTime now);
}
