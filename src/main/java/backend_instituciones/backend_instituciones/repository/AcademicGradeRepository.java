package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.AcademicGrade;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicGradeRepository extends JpaRepository<AcademicGrade, Long> {
    List<AcademicGrade> findByLevelIdOrderBySortOrderAscNameAsc(Long levelId);
    List<AcademicGrade> findByLevelIdInOrderBySortOrderAscNameAsc(List<Long> levelIds);
    Optional<AcademicGrade> findByIdAndLevelId(Long id, Long levelId);
    void deleteByLevelId(Long levelId);
}
