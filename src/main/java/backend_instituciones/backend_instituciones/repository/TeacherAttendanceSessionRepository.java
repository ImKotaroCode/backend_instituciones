package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.TeacherAttendanceSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface TeacherAttendanceSessionRepository extends JpaRepository<TeacherAttendanceSession, Long> {

    Optional<TeacherAttendanceSession> findByInstitutionIdAndCourseIdAndSectionIdAndTeacherIdAndAttendanceDate(
            Long institutionId, Long courseId, Long sectionId, Long teacherId, LocalDate attendanceDate);

    Optional<TeacherAttendanceSession> findByInstitutionIdAndTeacherIdAndAttendanceDateAndStartTimeLessThanEqualAndEndTimeGreaterThanEqual(
            Long institutionId, Long teacherId, LocalDate attendanceDate, LocalTime startTime, LocalTime endTime);

    Optional<TeacherAttendanceSession> findByIdAndInstitutionId(Long id, Long institutionId);

    @Query("SELECT s FROM TeacherAttendanceSession s WHERE s.institutionId = :institutionId " +
           "AND (:date IS NULL OR s.attendanceDate = :date) " +
           "AND (:dateFrom IS NULL OR s.attendanceDate >= :dateFrom) " +
           "AND (:dateTo IS NULL OR s.attendanceDate <= :dateTo) " +
           "AND (:teacherId IS NULL OR s.teacherId = :teacherId) " +
           "AND (:courseId IS NULL OR s.courseId = :courseId) " +
           "AND (:sectionId IS NULL OR s.sectionId = :sectionId) " +
           "AND (:levelId IS NULL OR s.levelId = :levelId) " +
           "AND (:gradeId IS NULL OR s.gradeId = :gradeId) " +
           "ORDER BY s.attendanceDate DESC, s.startTime ASC")
    List<TeacherAttendanceSession> findFiltered(
            @Param("institutionId") Long institutionId,
            @Param("date") LocalDate date,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("teacherId") Long teacherId,
            @Param("courseId") Long courseId,
            @Param("sectionId") Long sectionId,
            @Param("levelId") Long levelId,
            @Param("gradeId") Long gradeId);

    List<TeacherAttendanceSession> findByInstitutionIdAndTeacherIdAndAttendanceDateBetweenOrderByAttendanceDateAsc(
            Long institutionId, Long teacherId, LocalDate dateFrom, LocalDate dateTo);

    long countByInstitutionIdAndAttendanceDate(Long institutionId, LocalDate attendanceDate);
}
