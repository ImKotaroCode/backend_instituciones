package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.CourseContentAttachment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseContentAttachmentRepository extends JpaRepository<CourseContentAttachment, Long> {
    List<CourseContentAttachment> findByPostId(Long postId);
    List<CourseContentAttachment> findByPostIdIn(List<Long> postIds);
}
