package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.WarehouseMovement;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface WarehouseMovementRepository extends JpaRepository<WarehouseMovement, Long> {
    List<WarehouseMovement> findByInstitutionIdAndAssetIdOrderByOccurredAtDesc(Long institutionId, Long assetId);
    List<WarehouseMovement> findByInstitutionIdOrderByOccurredAtDesc(Long institutionId);
}
