package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.WarehouseSignature;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface WarehouseSignatureRepository extends JpaRepository<WarehouseSignature, Long> {
    Optional<WarehouseSignature> findByUserIdAndInstitutionId(Long userId, Long institutionId);
}
