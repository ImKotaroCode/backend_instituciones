package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.PaymentStudentProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentStudentProfileRepository extends JpaRepository<PaymentStudentProfile, Long> {

    List<PaymentStudentProfile> findByInstitutionIdOrderByStudentNameAsc(Long institutionId);

    List<PaymentStudentProfile> findByInstitutionIdAndAcademicYearIdOrderByStudentNameAsc(
            Long institutionId, Long academicYearId);

    Optional<PaymentStudentProfile> findByIdAndInstitutionId(Long id, Long institutionId);

    Optional<PaymentStudentProfile> findByInstitutionIdAndStudentIdAndAcademicYearId(
            Long institutionId, Long studentId, Long academicYearId);

    List<PaymentStudentProfile> findByInstitutionIdAndStudentId(Long institutionId, Long studentId);
}
