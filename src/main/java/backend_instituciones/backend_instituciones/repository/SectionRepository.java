package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.Section;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SectionRepository extends JpaRepository<Section, Long> {
    List<Section> findByInstitutionIdOrderByNameAsc(Long institutionId);
    Optional<Section> findByIdAndInstitutionId(Long id, Long institutionId);
    boolean existsByNameAndInstitutionId(String name, Long institutionId);
    Optional<Section> findByInstitutionIdAndName(Long institutionId, String name);
}
