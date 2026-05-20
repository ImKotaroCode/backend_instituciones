package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.WarehouseLoan;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.domain.Pageable;

public interface WarehouseLoanRepository extends JpaRepository<WarehouseLoan, Long> {
    List<WarehouseLoan> findByInstitutionIdAndAssetIdOrderByOutAtDesc(Long institutionId, Long assetId);
    List<WarehouseLoan> findByInstitutionIdOrderByOutAtDesc(Long institutionId);
    Optional<WarehouseLoan> findByIdAndInstitutionId(Long id, Long institutionId);

    /** Overdue: ENTREGADO and past dueAt */
    @Query("SELECT l FROM WarehouseLoan l WHERE l.institutionId = :institutionId " +
           "AND l.status = 'ENTREGADO' AND l.dueAt < :now")
    List<WarehouseLoan> findOverdue(@Param("institutionId") Long institutionId,
                                    @Param("now") LocalDateTime now);

    long countByInstitutionIdAndStatus(Long institutionId, String status);

    @Query("SELECT COUNT(l) FROM WarehouseLoan l WHERE l.institutionId = :institutionId " +
           "AND l.status = 'ENTREGADO' AND l.dueAt < :now")
    long countOverdue(@Param("institutionId") Long institutionId,
                      @Param("now") LocalDateTime now);

    @Query("SELECT l FROM WarehouseLoan l WHERE l.institutionId = :institutionId " +
           "AND l.status = 'ENTREGADO' AND l.dueAt < :now")
    List<WarehouseLoan> findOverdue(@Param("institutionId") Long institutionId,
                                    @Param("now") LocalDateTime now,
                                    Pageable pageable);
}
