package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.ClassroomStudent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassroomStudentRepository extends JpaRepository<ClassroomStudent, Long> {
    List<ClassroomStudent> findByClassroomId(Long classroomId);
    Optional<ClassroomStudent> findByClassroomIdAndStudentId(Long classroomId, Long studentId);
    boolean existsByClassroomIdAndStudentId(Long classroomId, Long studentId);
    void deleteByClassroomIdAndStudentId(Long classroomId, Long studentId);
    List<ClassroomStudent> findByStudentIdAndInstitutionId(Long studentId, Long institutionId);
}
