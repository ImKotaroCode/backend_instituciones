package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.WarehousePurchaseIncident;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehousePurchaseIncidentRepository extends JpaRepository<WarehousePurchaseIncident, Long> {
    List<WarehousePurchaseIncident> findByInstitutionIdAndPurchaseOrderIdOrderByReportedAtDesc(
            Long institutionId, Long purchaseOrderId);
    List<WarehousePurchaseIncident> findByInstitutionIdOrderByReportedAtDesc(Long institutionId);
    Optional<WarehousePurchaseIncident> findByIdAndInstitutionId(Long id, Long institutionId);
    long countByInstitutionIdAndStatus(Long institutionId, String status);
}
