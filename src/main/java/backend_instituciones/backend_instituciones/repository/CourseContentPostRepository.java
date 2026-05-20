package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.CourseContentPost;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CourseContentPostRepository extends JpaRepository<CourseContentPost, Long> {
    List<CourseContentPost> findByInstitutionIdAndCourseIdOrderByPublishedAtDesc(Long institutionId, Long courseId);
}
