package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.PaymentEnrollmentType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentEnrollmentTypeRepository extends JpaRepository<PaymentEnrollmentType, Long> {
    List<PaymentEnrollmentType> findByInstitutionIdOrderByNameAsc(Long institutionId);
    Optional<PaymentEnrollmentType> findByIdAndInstitutionId(Long id, Long institutionId);
}
