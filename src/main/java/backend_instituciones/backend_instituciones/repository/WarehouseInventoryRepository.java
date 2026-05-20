package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.WarehouseInventory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseInventoryRepository extends JpaRepository<WarehouseInventory, Long> {
    List<WarehouseInventory> findByInstitutionIdOrderByCountedAtDesc(Long institutionId);
    Optional<WarehouseInventory> findByIdAndInstitutionId(Long id, Long institutionId);
}
