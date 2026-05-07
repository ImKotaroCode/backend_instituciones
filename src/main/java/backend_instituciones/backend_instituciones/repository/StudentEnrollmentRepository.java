package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.StudentEnrollment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentEnrollmentRepository extends JpaRepository<StudentEnrollment, Long> {
    Page<StudentEnrollment> findByInstitutionId(Long institutionId, Pageable pageable);
    List<StudentEnrollment> findByStudentIdAndInstitutionId(Long studentId, Long institutionId);
    List<StudentEnrollment> findByClassroomIdAndAcademicYearId(Long classroomId, Long academicYearId);
    Optional<StudentEnrollment> findByStudentIdAndAcademicYearId(Long studentId, Long academicYearId);
    Optional<StudentEnrollment> findByIdAndInstitutionId(Long id, Long institutionId);
    boolean existsByStudentIdAndAcademicYearId(Long studentId, Long academicYearId);

    @Query("SELECT e FROM StudentEnrollment e WHERE e.institutionId = :instId AND e.academicYearId = :yearId")
    List<StudentEnrollment> findByAcademicYearId(@Param("instId") Long institutionId, @Param("yearId") Long academicYearId);

    @Query("SELECT COUNT(e) FROM StudentEnrollment e WHERE e.classroomId = :classroomId AND e.academicYearId = :yearId")
    long countByClassroomIdAndAcademicYearId(@Param("classroomId") Long classroomId, @Param("yearId") Long academicYearId);
}
