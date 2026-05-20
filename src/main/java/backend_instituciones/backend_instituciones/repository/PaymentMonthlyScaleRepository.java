package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.PaymentMonthlyScale;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface PaymentMonthlyScaleRepository extends JpaRepository<PaymentMonthlyScale, Long> {
    List<PaymentMonthlyScale> findByInstitutionIdOrderByNameAsc(Long institutionId);
    Optional<PaymentMonthlyScale> findByIdAndInstitutionId(Long id, Long institutionId);
}
