package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.Classroom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ClassroomRepository extends JpaRepository<Classroom, Long> {
    Page<Classroom> findByInstitutionId(Long institutionId, Pageable pageable);
    Page<Classroom> findByInstitutionIdAndAcademicYear(Long institutionId, String academicYear, Pageable pageable);
    Optional<Classroom> findByIdAndInstitutionId(Long id, Long institutionId);
    Optional<Classroom> findByInstitutionIdAndAcademicYearIdAndGradeLevelIdAndSectionId(
            Long institutionId, Long academicYearId, Long gradeLevelId, Long sectionId);
    List<Classroom> findByInstitutionIdAndAcademicYearId(Long institutionId, Long academicYearId);
    List<Classroom> findByInstitutionId(Long institutionId);
    Optional<Classroom> findByInstitutionIdAndAcademicYearIdAndAcademicLevelIdAndAcademicGradeIdAndAcademicSectionId(
            Long institutionId, Long academicYearId, Long academicLevelId, Long academicGradeId, Long academicSectionId);

    List<Classroom> findByInstitutionIdAndAcademicLevelIdAndAcademicGradeIdAndAcademicSectionId(
            Long institutionId, Long academicLevelId, Long academicGradeId, Long academicSectionId);
}
