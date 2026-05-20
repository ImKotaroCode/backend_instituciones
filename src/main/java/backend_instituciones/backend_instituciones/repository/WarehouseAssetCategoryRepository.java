package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.WarehouseAssetCategory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseAssetCategoryRepository extends JpaRepository<WarehouseAssetCategory, Long> {
    List<WarehouseAssetCategory> findByInstitutionIdOrderByMainCategoryAscNameAsc(Long institutionId);
    Optional<WarehouseAssetCategory> findByIdAndInstitutionId(Long id, Long institutionId);
    boolean existsByIdAndInstitutionId(Long id, Long institutionId);
}
