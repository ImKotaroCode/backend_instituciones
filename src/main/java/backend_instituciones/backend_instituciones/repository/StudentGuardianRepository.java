package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.StudentGuardian;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface StudentGuardianRepository extends JpaRepository<StudentGuardian, Long> {
    List<StudentGuardian> findByStudentIdAndInstitutionId(Long studentId, Long institutionId);
    List<StudentGuardian> findByGuardianIdAndInstitutionId(Long guardianId, Long institutionId);
    Optional<StudentGuardian> findByStudentIdAndGuardianId(Long studentId, Long guardianId);
    boolean existsByStudentIdAndGuardianId(Long studentId, Long guardianId);
    void deleteByStudentIdAndGuardianId(Long studentId, Long guardianId);
}
