package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.WarehousePurchaseQuotation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehousePurchaseQuotationRepository extends JpaRepository<WarehousePurchaseQuotation, Long> {
    List<WarehousePurchaseQuotation> findByInstitutionIdAndPurchaseOrderIdOrderByCreatedAtDesc(
            Long institutionId, Long purchaseOrderId);
    Optional<WarehousePurchaseQuotation> findByIdAndInstitutionId(Long id, Long institutionId);
    List<WarehousePurchaseQuotation> findByInstitutionIdAndPurchaseOrderId(Long institutionId, Long purchaseOrderId);
    List<WarehousePurchaseQuotation> findByInstitutionIdOrderByCreatedAtDesc(Long institutionId);
}
