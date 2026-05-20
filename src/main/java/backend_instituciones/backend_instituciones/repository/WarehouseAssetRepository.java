package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.WarehouseAsset;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface WarehouseAssetRepository extends JpaRepository<WarehouseAsset, Long> {

    Optional<WarehouseAsset> findByIdAndInstitutionId(Long id, Long institutionId);

    long countByInstitutionId(Long institutionId);

    long countByInstitutionIdAndStatusIn(Long institutionId, List<String> statuses);

    @Query("SELECT a FROM WarehouseAsset a WHERE a.institutionId = :institutionId " +
           "AND (:roomId IS NULL OR a.roomId = :roomId) " +
           "AND (:categoryId IS NULL OR a.categoryId = :categoryId) " +
           "AND (:status IS NULL OR a.status = :status) " +
           "AND (:q IS NULL OR LOWER(a.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "     OR LOWER(COALESCE(a.code,'')) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "     OR LOWER(COALESCE(a.serialNumber,'')) LIKE LOWER(CONCAT('%', :q, '%'))) " +
           "ORDER BY a.createdAt DESC")
    List<WarehouseAsset> search(@Param("institutionId") Long institutionId,
                                @Param("roomId") Long roomId,
                                @Param("categoryId") Long categoryId,
                                @Param("status") String status,
                                @Param("q") String q);

    /** For report by category name */
    @Query("SELECT a.category, COUNT(a) FROM WarehouseAsset a " +
           "WHERE a.institutionId = :institutionId AND a.category IS NOT NULL " +
           "GROUP BY a.category ORDER BY COUNT(a) DESC")
    List<Object[]> countByCategory(@Param("institutionId") Long institutionId);

    /** For report by room */
    @Query("SELECT a.roomName, COUNT(a) FROM WarehouseAsset a " +
           "WHERE a.institutionId = :institutionId AND a.roomName IS NOT NULL " +
           "GROUP BY a.roomName ORDER BY COUNT(a) DESC")
    List<Object[]> countByRoom(@Param("institutionId") Long institutionId);

    List<WarehouseAsset> findByInstitutionIdAndRoomId(Long institutionId, Long roomId);

    long countByInstitutionIdAndCategoryId(Long institutionId, Long categoryId);
}
