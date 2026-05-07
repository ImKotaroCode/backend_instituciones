package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.AcademicLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AcademicLevelRepository extends JpaRepository<AcademicLevel, Long> {
    List<AcademicLevel> findByInstitutionIdOrderBySortOrderAscNameAsc(Long institutionId);
    Optional<AcademicLevel> findByIdAndInstitutionId(Long id, Long institutionId);
    boolean existsByInstitutionIdAndName(Long institutionId, String name);
}
