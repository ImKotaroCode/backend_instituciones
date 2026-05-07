package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.CourseAssignment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CourseAssignmentRepository extends JpaRepository<CourseAssignment, Long> {
    Page<CourseAssignment> findByInstitutionId(Long institutionId, Pageable pageable);
    List<CourseAssignment> findByClassroomId(Long classroomId);
    List<CourseAssignment> findByClassroomIdAndInstitutionId(Long classroomId, Long institutionId);
    List<CourseAssignment> findByTeacherId(Long teacherId);
    Optional<CourseAssignment> findByIdAndInstitutionId(Long id, Long institutionId);
    boolean existsByClassroomIdAndCourseId(Long classroomId, Long courseId);
    boolean existsByGeneratedCode(String generatedCode);
    List<CourseAssignment> findByClassroomIdInAndInstitutionId(List<Long> classroomIds, Long institutionId);
}
