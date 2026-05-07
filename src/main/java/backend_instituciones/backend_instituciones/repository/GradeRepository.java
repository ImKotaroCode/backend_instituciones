package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.Grade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeRepository extends JpaRepository<Grade, Long> {
    List<Grade> findByCourseIdAndInstitutionId(Long courseId, Long institutionId);
    List<Grade> findByStudentIdAndInstitutionId(Long studentId, Long institutionId);
    Optional<Grade> findByIdAndInstitutionId(Long id, Long institutionId);
    List<Grade> findByStudentIdAndCourseIdAndInstitutionId(Long studentId, Long courseId, Long institutionId);
}
