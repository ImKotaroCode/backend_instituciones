package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.AcademicSection;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicSectionRepository extends JpaRepository<AcademicSection, Long> {
    List<AcademicSection> findByGradeIdOrderBySortOrderAscNameAsc(Long gradeId);
    Optional<AcademicSection> findByIdAndGradeId(Long id, Long gradeId);
    void deleteByGradeId(Long gradeId);
    List<AcademicSection> findByGradeIdIn(List<Long> gradeIds);
}
