package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.CourseAssessmentScore;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseAssessmentScoreRepository extends JpaRepository<CourseAssessmentScore, Long> {
    List<CourseAssessmentScore> findByAssessmentId(Long assessmentId);
    Optional<CourseAssessmentScore> findByAssessmentIdAndStudentId(Long assessmentId, Long studentId);
    List<CourseAssessmentScore> findByAssessmentIdIn(List<Long> assessmentIds);
    List<CourseAssessmentScore> findByAssessmentIdAndStudentIdIn(Long assessmentId, List<Long> studentIds);

    List<CourseAssessmentScore> findByAssessmentIdInAndStudentId(List<Long> assessmentIds, Long studentId);
}
