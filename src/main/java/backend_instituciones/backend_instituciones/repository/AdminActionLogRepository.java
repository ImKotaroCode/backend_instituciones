package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.AdminActionLog;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface AdminActionLogRepository extends JpaRepository<AdminActionLog, Long> {

    @Query("""
        SELECT l FROM AdminActionLog l
        WHERE l.institutionId = :institutionId
          AND (:userId IS NULL OR l.userId = :userId)
          AND (:module IS NULL OR l.module = :module)
          AND (:dateFrom IS NULL OR l.createdAt >= :dateFrom)
          AND (:dateTo IS NULL OR l.createdAt <= :dateTo)
        ORDER BY l.createdAt DESC
        """)
    Page<AdminActionLog> search(
            @Param("institutionId") Long institutionId,
            @Param("userId") Long userId,
            @Param("module") String module,
            @Param("dateFrom") LocalDateTime dateFrom,
            @Param("dateTo") LocalDateTime dateTo,
            Pageable pageable);
}
