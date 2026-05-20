package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.PaymentVoucherSubmission;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentVoucherSubmissionRepository extends JpaRepository<PaymentVoucherSubmission, Long> {

    Optional<PaymentVoucherSubmission> findByChargeId(Long chargeId);

    Optional<PaymentVoucherSubmission> findByIdAndInstitutionId(Long id, Long institutionId);

    /** Parent: all vouchers for a student */
    List<PaymentVoucherSubmission> findByInstitutionIdAndStudentIdOrderBySubmittedAtDesc(
            Long institutionId, Long studentId);

    /** Parent: vouchers for a student filtered by academic year */
    List<PaymentVoucherSubmission> findByInstitutionIdAndStudentIdAndAcademicYearIdOrderBySubmittedAtDesc(
            Long institutionId, Long studentId, Long academicYearId);

    /** Admin: all SUBMITTED vouchers for institution+year (for pending panel) */
    List<PaymentVoucherSubmission> findByInstitutionIdAndAcademicYearIdAndStatusOrderBySubmittedAtDesc(
            Long institutionId, Long academicYearId, String status);

    /** Admin: all vouchers for institution+year (for detail/filter view) */
    List<PaymentVoucherSubmission> findByInstitutionIdAndAcademicYearIdOrderBySubmittedAtDesc(
            Long institutionId, Long academicYearId);

    /** Admin: vouchers for specific student */
    List<PaymentVoucherSubmission> findByInstitutionIdAndStudentIdAndAcademicYearIdAndStatusOrderBySubmittedAtDesc(
            Long institutionId, Long studentId, Long academicYearId, String status);

    /** Parent dashboard: all vouchers for multiple students */
    List<PaymentVoucherSubmission> findByInstitutionIdAndStudentIdIn(
            Long institutionId, List<Long> studentIds);
}
