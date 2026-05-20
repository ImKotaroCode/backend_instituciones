package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.CourseTaskSubmissionFile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseTaskSubmissionFileRepository extends JpaRepository<CourseTaskSubmissionFile, Long> {
    List<CourseTaskSubmissionFile> findBySubmissionId(Long submissionId);
    List<CourseTaskSubmissionFile> findBySubmissionIdIn(List<Long> submissionIds);
}
