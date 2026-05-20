package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.WarehouseMaintenance;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseMaintenanceRepository extends JpaRepository<WarehouseMaintenance, Long> {
    List<WarehouseMaintenance> findByInstitutionIdAndAssetIdOrderByScheduledAtDesc(Long institutionId, Long assetId);
    List<WarehouseMaintenance> findByInstitutionIdOrderByScheduledAtDesc(Long institutionId);
    List<WarehouseMaintenance> findByInstitutionIdAndStatus(Long institutionId, String status);
    Optional<WarehouseMaintenance> findByIdAndInstitutionId(Long id, Long institutionId);
    long countByInstitutionIdAndStatus(Long institutionId, String status);
}
