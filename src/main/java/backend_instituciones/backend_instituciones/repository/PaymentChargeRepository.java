package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.PaymentCharge;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface PaymentChargeRepository extends JpaRepository<PaymentCharge, Long> {

    List<PaymentCharge> findByInstitutionIdAndAcademicYearIdOrderByDueDateAsc(
            Long institutionId, Long academicYearId);

    List<PaymentCharge> findByInstitutionIdAndStudentIdAndAcademicYearIdOrderByDueDateAsc(
            Long institutionId, Long studentId, Long academicYearId);

    List<PaymentCharge> findByInstitutionIdAndStudentIdOrderByDueDateAsc(
            Long institutionId, Long studentId);

    List<PaymentCharge> findByProfileIdOrderByDueDateAsc(Long profileId);

    Optional<PaymentCharge> findByIdAndInstitutionId(Long id, Long institutionId);

    boolean existsByProfileIdAndKind(Long profileId, String kind);

    boolean existsByProfileIdAndKindAndMonthKey(Long profileId, String kind, String monthKey);

    @Query("SELECT COALESCE(SUM(c.baseAmount), 0) FROM PaymentCharge c " +
           "WHERE c.institutionId = :institutionId AND c.academicYearId = :yearId AND c.status = 'PENDING'")
    BigDecimal sumPendingAmount(@Param("institutionId") Long institutionId, @Param("yearId") Long yearId);

    @Query("SELECT COUNT(c) FROM PaymentCharge c " +
           "WHERE c.institutionId = :institutionId AND c.academicYearId = :yearId AND c.status = 'PENDING'")
    long countPending(@Param("institutionId") Long institutionId, @Param("yearId") Long yearId);

    @Query("SELECT COUNT(c) FROM PaymentCharge c " +
           "WHERE c.institutionId = :institutionId AND c.academicYearId = :yearId " +
           "AND c.status = 'PENDING' AND c.dueDate < :today")
    long countOverdue(@Param("institutionId") Long institutionId, @Param("yearId") Long yearId,
                      @Param("today") LocalDate today);
}
