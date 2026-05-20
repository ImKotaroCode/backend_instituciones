package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.WarehousePavilion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehousePavilionRepository extends JpaRepository<WarehousePavilion, Long> {
    List<WarehousePavilion> findByInstitutionIdOrderByNameAsc(Long institutionId);
    List<WarehousePavilion> findByInstitutionIdAndSectorIdOrderByNameAsc(Long institutionId, Long sectorId);
    Optional<WarehousePavilion> findByIdAndInstitutionId(Long id, Long institutionId);
}
