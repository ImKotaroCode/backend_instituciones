package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.WarehouseSector;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseSectorRepository extends JpaRepository<WarehouseSector, Long> {
    List<WarehouseSector> findByInstitutionIdOrderByNameAsc(Long institutionId);
    Optional<WarehouseSector> findByIdAndInstitutionId(Long id, Long institutionId);
}
