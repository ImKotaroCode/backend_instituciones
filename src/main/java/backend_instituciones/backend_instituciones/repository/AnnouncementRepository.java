package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.Announcement;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import org.springframework.data.domain.Pageable;
import java.util.List;
import java.util.Optional;

public interface AnnouncementRepository extends JpaRepository<Announcement, Long> {

    Page<Announcement> findByInstitutionIdOrderByCreatedAtDesc(Long institutionId, Pageable pageable);

    Optional<Announcement> findByIdAndInstitutionId(Long id, Long institutionId);

    /** Admin: filter by monthKey (optional) */
    List<Announcement> findByInstitutionIdAndMonthKeyOrderByCreatedAtDesc(Long institutionId, String monthKey);

    List<Announcement> findByInstitutionIdOrderByCreatedAtDesc(Long institutionId);

    /** Inbox: only PUBLICADO, optionally by monthKey */
    List<Announcement> findByInstitutionIdAndStatusAndMonthKeyOrderByPublishedAtDesc(
            Long institutionId, String status, String monthKey);

    List<Announcement> findByInstitutionIdAndStatusOrderByPublishedAtDesc(Long institutionId, String status);

    long countByInstitutionIdAndStatusAndMonthKey(Long institutionId, String status, String monthKey);

    List<Announcement> findByInstitutionIdAndStatusAndMonthKeyOrderByPublishedAtDesc(
            Long institutionId, String status, String monthKey, Pageable pageable);

    /** Targeted inbox: PUBLICADO + role substring match + optional monthKey + optional kind */
    @Query("SELECT a FROM Announcement a WHERE a.institutionId = :institutionId " +
           "AND a.status = 'PUBLICADO' " +
           "AND a.targetRoles LIKE %:role% " +
           "AND (:monthKey IS NULL OR a.monthKey = :monthKey) " +
           "AND (:kind IS NULL OR a.kind = :kind) " +
           "ORDER BY a.publishedAt DESC")
    List<Announcement> findInboxForRole(@Param("institutionId") Long institutionId,
                                        @Param("role") String role,
                                        @Param("monthKey") String monthKey,
                                        @Param("kind") String kind);
}
