package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.AnnouncementView;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnnouncementViewRepository extends JpaRepository<AnnouncementView, Long> {

    Optional<AnnouncementView> findByAnnouncementIdAndUserId(Long announcementId, Long userId);

    /** Batch-load all views for a user across multiple announcements */
    List<AnnouncementView> findByUserIdAndAnnouncementIdIn(Long userId, List<Long> announcementIds);
}
