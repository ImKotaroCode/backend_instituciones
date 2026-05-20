package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.WarehouseSupplier;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseSupplierRepository extends JpaRepository<WarehouseSupplier, Long> {
    List<WarehouseSupplier> findByInstitutionIdOrderByNameAsc(Long institutionId);
    Optional<WarehouseSupplier> findByIdAndInstitutionId(Long id, Long institutionId);
}
