package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.ImportJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ImportJobRepository extends JpaRepository<ImportJob, String> {
    Optional<ImportJob> findByIdAndInstitutionId(String id, Long institutionId);
}
