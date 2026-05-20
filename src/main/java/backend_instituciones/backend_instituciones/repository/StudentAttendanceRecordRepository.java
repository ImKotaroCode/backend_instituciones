package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.StudentAttendanceRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;

public interface StudentAttendanceRecordRepository extends JpaRepository<StudentAttendanceRecord, Long> {

    List<StudentAttendanceRecord> findBySessionId(Long sessionId);

    List<StudentAttendanceRecord> findBySessionIdIn(java.util.Collection<Long> sessionIds);

    @Modifying
    @Transactional
    @Query("DELETE FROM StudentAttendanceRecord r WHERE r.sessionId = :sessionId")
    void deleteBySessionId(@Param("sessionId") Long sessionId);

    @Query("SELECT r FROM StudentAttendanceRecord r WHERE r.institutionId = :institutionId " +
           "AND (:date IS NULL OR r.attendanceDate = :date) " +
           "AND (:dateFrom IS NULL OR r.attendanceDate >= :dateFrom) " +
           "AND (:dateTo IS NULL OR r.attendanceDate <= :dateTo) " +
           "AND (:studentId IS NULL OR r.studentId = :studentId) " +
           "AND (:courseId IS NULL OR r.courseId = :courseId) " +
           "AND (:sectionId IS NULL OR r.sectionId = :sectionId) " +
           "AND (:teacherId IS NULL OR r.teacherId = :teacherId) " +
           "ORDER BY r.attendanceDate DESC, r.startTime ASC")
    List<StudentAttendanceRecord> findFiltered(
            @Param("institutionId") Long institutionId,
            @Param("date") LocalDate date,
            @Param("dateFrom") LocalDate dateFrom,
            @Param("dateTo") LocalDate dateTo,
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("sectionId") Long sectionId,
            @Param("teacherId") Long teacherId);

    List<StudentAttendanceRecord> findByInstitutionIdAndStudentIdAndAttendanceDateBetween(
            Long institutionId, Long studentId, LocalDate dateFrom, LocalDate dateTo);

    long countByInstitutionIdAndAttendanceDate(Long institutionId, LocalDate attendanceDate);

    @Query("SELECT r FROM StudentAttendanceRecord r WHERE r.institutionId = :institutionId " +
           "AND r.studentId = :studentId AND r.courseId = :courseId " +
           "AND (:sectionId IS NULL OR r.sectionId = :sectionId) " +
           "AND r.attendanceDate = :date AND r.startTime = :startTime " +
           "ORDER BY r.id DESC")
    List<StudentAttendanceRecord> findExisting(
            @Param("institutionId") Long institutionId,
            @Param("studentId") Long studentId,
            @Param("courseId") Long courseId,
            @Param("sectionId") Long sectionId,
            @Param("date") LocalDate date,
            @Param("startTime") LocalTime startTime);
}
