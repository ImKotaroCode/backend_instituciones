package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.StudentAcademicStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StudentAcademicStatusRepository extends JpaRepository<StudentAcademicStatus, Long> {
    List<StudentAcademicStatus> findByStudentIdAndInstitutionIdOrderByAcademicYearIdDesc(Long studentId, Long institutionId);
    Optional<StudentAcademicStatus> findByStudentIdAndAcademicYearId(Long studentId, Long academicYearId);

    @Query("SELECT s FROM StudentAcademicStatus s WHERE s.institutionId = :instId AND s.academicYearId = :yearId AND s.status = :status")
    List<StudentAcademicStatus> findByAcademicYearIdAndStatus(
            @Param("instId") Long institutionId,
            @Param("yearId") Long academicYearId,
            @Param("status") String status);

    @Query("SELECT COUNT(s) FROM StudentAcademicStatus s WHERE s.institutionId = :instId AND s.academicYearId = :yearId AND s.status = :status")
    long countByAcademicYearIdAndStatus(
            @Param("instId") Long institutionId,
            @Param("yearId") Long academicYearId,
            @Param("status") String status);

    @Query("SELECT COUNT(s) FROM StudentAcademicStatus s WHERE s.institutionId = :instId AND s.academicYearId = :yearId")
    long countByAcademicYearId(@Param("instId") Long institutionId, @Param("yearId") Long academicYearId);
}
