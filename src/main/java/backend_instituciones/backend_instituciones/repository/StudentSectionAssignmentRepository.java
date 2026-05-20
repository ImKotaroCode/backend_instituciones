package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.StudentSectionAssignment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentSectionAssignmentRepository extends JpaRepository<StudentSectionAssignment, Long> {
    Optional<StudentSectionAssignment> findByInstitutionIdAndStudentId(Long institutionId, Long studentId);

    List<StudentSectionAssignment> findByInstitutionIdAndLevelIdAndGradeIdAndSectionId(
            Long institutionId, Long levelId, Long gradeId, Long sectionId);
}
