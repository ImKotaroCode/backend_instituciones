package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.AcademicYear;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AcademicYearRepository extends JpaRepository<AcademicYear, Long> {
    List<AcademicYear> findByInstitutionIdOrderByNameDesc(Long institutionId);
    Optional<AcademicYear> findByIdAndInstitutionId(Long id, Long institutionId);
    Optional<AcademicYear> findByInstitutionIdAndIsCurrentTrue(Long institutionId);
    boolean existsByNameAndInstitutionId(String name, Long institutionId);

    @Modifying
    @Query("UPDATE AcademicYear a SET a.isCurrent = false WHERE a.institutionId = :institutionId")
    void clearCurrentForInstitution(@Param("institutionId") Long institutionId);
}
