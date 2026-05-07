package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.GradeAudit;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GradeAuditRepository extends JpaRepository<GradeAudit, Long> {
    List<GradeAudit> findByGradeIdOrderByChangedAtDesc(Long gradeId);
}
