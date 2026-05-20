package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.AcademicPeriod;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

public interface AcademicPeriodRepository extends JpaRepository<AcademicPeriod, Long> {

    List<AcademicPeriod> findByInstitutionIdOrderBySortOrderAsc(Long institutionId);

    Optional<AcademicPeriod> findByIdAndInstitutionId(Long id, Long institutionId);

    @Query("SELECT p FROM AcademicPeriod p WHERE p.institutionId = :institutionId " +
           "AND p.startDate <= :date AND p.endDate >= :date ORDER BY p.sortOrder ASC")
    List<AcademicPeriod> findByInstitutionIdAndDate(
            @Param("institutionId") Long institutionId,
            @Param("date") LocalDate date);

    @Modifying
    @Query("DELETE FROM AcademicPeriod p WHERE p.institutionId = :institutionId")
    void deleteByInstitutionId(@Param("institutionId") Long institutionId);
}
