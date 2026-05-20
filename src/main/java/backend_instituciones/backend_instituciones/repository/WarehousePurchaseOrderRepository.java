package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.WarehousePurchaseOrder;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehousePurchaseOrderRepository extends JpaRepository<WarehousePurchaseOrder, Long> {
    List<WarehousePurchaseOrder> findByInstitutionIdOrderByCreatedAtDesc(Long institutionId);
    Optional<WarehousePurchaseOrder> findByIdAndInstitutionId(Long id, Long institutionId);
    long countByInstitutionId(Long institutionId);
}
