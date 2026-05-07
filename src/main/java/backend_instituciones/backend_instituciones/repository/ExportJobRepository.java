package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.ExportJob;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface ExportJobRepository extends JpaRepository<ExportJob, Long> {
    Optional<ExportJob> findByIdAndInstitutionId(Long id, Long institutionId);
}
