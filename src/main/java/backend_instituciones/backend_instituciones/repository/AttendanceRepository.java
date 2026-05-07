package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.Attendance;
import backend_instituciones.backend_instituciones.domain.enums.AttendanceStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AttendanceRepository extends JpaRepository<Attendance, Long> {
    List<Attendance> findByCourseIdAndDate(Long courseId, LocalDate date);
    List<Attendance> findByStudentIdAndInstitutionId(Long studentId, Long institutionId);
    Optional<Attendance> findByCourseIdAndStudentIdAndDate(Long courseId, Long studentId, LocalDate date);

    @Query("SELECT COUNT(a) FROM Attendance a WHERE a.studentId = :studentId AND a.institutionId = :institutionId AND a.status = :status")
    long countByStudentIdAndInstitutionIdAndStatus(@Param("studentId") Long studentId,
                                                    @Param("institutionId") Long institutionId,
                                                    @Param("status") AttendanceStatus status);
}
