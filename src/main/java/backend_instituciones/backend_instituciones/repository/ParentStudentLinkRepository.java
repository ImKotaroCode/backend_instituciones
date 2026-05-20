package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.ParentStudentLink;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

public interface ParentStudentLinkRepository extends JpaRepository<ParentStudentLink, Long> {
    List<ParentStudentLink> findByInstitutionIdAndParentId(Long institutionId, Long parentId);
    List<ParentStudentLink> findByInstitutionIdAndStudentId(Long institutionId, Long studentId);
    boolean existsByInstitutionIdAndParentIdAndStudentId(Long institutionId, Long parentId, Long studentId);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Transactional
    @Query("DELETE FROM ParentStudentLink p WHERE p.institutionId = :institutionId AND p.parentId = :parentId")
    void deleteByInstitutionIdAndParentId(@Param("institutionId") Long institutionId,
                                          @Param("parentId") Long parentId);
}
