package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.CourseTaskSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseTaskSubmissionRepository extends JpaRepository<CourseTaskSubmission, Long> {
    List<CourseTaskSubmission> findByTaskId(Long taskId);
    List<CourseTaskSubmission> findByTaskIdAndStudentId(Long taskId, Long studentId);
    List<CourseTaskSubmission> findByTaskIdAndGroupId(Long taskId, Long groupId);
    Optional<CourseTaskSubmission> findByTaskIdAndStudentIdAndGroupIdIsNull(Long taskId, Long studentId);
    Optional<CourseTaskSubmission> findByIdAndInstitutionId(Long id, Long institutionId);
    List<CourseTaskSubmission> findByTaskIdIn(List<Long> taskIds);
    List<CourseTaskSubmission> findByTaskIdInAndStudentId(List<Long> taskIds, Long studentId);
    List<CourseTaskSubmission> findByTaskIdInAndGroupIdIn(List<Long> taskIds, List<Long> groupIds);
}
