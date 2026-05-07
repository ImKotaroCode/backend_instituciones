package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.InstitutionConfig;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface InstitutionConfigRepository extends JpaRepository<InstitutionConfig, Long> {
    Optional<InstitutionConfig> findByInstitutionId(Long institutionId);
}
