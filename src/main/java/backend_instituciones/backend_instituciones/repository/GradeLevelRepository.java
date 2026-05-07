package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.GradeLevel;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GradeLevelRepository extends JpaRepository<GradeLevel, Long> {
    List<GradeLevel> findByInstitutionIdOrderByOrderIndexAscNameAsc(Long institutionId);
    Optional<GradeLevel> findByIdAndInstitutionId(Long id, Long institutionId);
    boolean existsByNameAndInstitutionId(String name, Long institutionId);
    Optional<GradeLevel> findByInstitutionIdAndLevelAndName(Long institutionId, String level, String name);
}
