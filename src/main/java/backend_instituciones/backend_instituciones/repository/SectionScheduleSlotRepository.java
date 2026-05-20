package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.SectionScheduleSlot;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface SectionScheduleSlotRepository extends JpaRepository<SectionScheduleSlot, Long> {
    List<SectionScheduleSlot> findByInstitutionIdAndSectionId(Long institutionId, Long sectionId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM SectionScheduleSlot s WHERE s.institutionId = :institutionId AND s.sectionId = :sectionId")
    void deleteByInstitutionIdAndSectionId(@Param("institutionId") Long institutionId, @Param("sectionId") Long sectionId);

    List<SectionScheduleSlot> findByInstitutionIdAndTeacherId(Long institutionId, Long teacherId);

    List<SectionScheduleSlot> findByInstitutionId(Long institutionId);

    List<SectionScheduleSlot> findByInstitutionIdAndSectionIdIn(Long institutionId, java.util.Collection<Long> sectionIds);

    List<SectionScheduleSlot> findByInstitutionIdAndCourseId(Long institutionId, Long courseId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("UPDATE SectionScheduleSlot s SET s.teacherId = :teacherId WHERE s.courseId = :courseId")
    int updateTeacherIdByCourseId(@Param("courseId") Long courseId, @Param("teacherId") Long teacherId);

    @Query("SELECT s FROM SectionScheduleSlot s WHERE s.institutionId = :institutionId " +
           "AND s.weekday = :weekday " +
           "AND (:levelId IS NULL OR s.levelId = :levelId) " +
           "AND (:gradeId IS NULL OR s.gradeId = :gradeId) " +
           "AND (:sectionId IS NULL OR s.sectionId = :sectionId) " +
           "AND (:teacherId IS NULL OR s.teacherId = :teacherId)")
    List<SectionScheduleSlot> findForAlerts(
            @Param("institutionId") Long institutionId,
            @Param("weekday") String weekday,
            @Param("levelId") Long levelId,
            @Param("gradeId") Long gradeId,
            @Param("sectionId") Long sectionId,
            @Param("teacherId") Long teacherId);
}
