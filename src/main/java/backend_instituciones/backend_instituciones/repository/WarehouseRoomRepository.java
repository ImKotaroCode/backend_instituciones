package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.WarehouseRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface WarehouseRoomRepository extends JpaRepository<WarehouseRoom, Long> {
    List<WarehouseRoom> findByInstitutionIdOrderByNameAsc(Long institutionId);
    List<WarehouseRoom> findByInstitutionIdAndSectorIdOrderByNameAsc(Long institutionId, Long sectorId);
    List<WarehouseRoom> findByInstitutionIdAndPavilionIdOrderByNameAsc(Long institutionId, Long pavilionId);
    List<WarehouseRoom> findByInstitutionIdAndSectorIdAndPavilionIdOrderByNameAsc(Long institutionId, Long sectorId, Long pavilionId);
    Optional<WarehouseRoom> findByIdAndInstitutionId(Long id, Long institutionId);
    long countByInstitutionId(Long institutionId);
}
