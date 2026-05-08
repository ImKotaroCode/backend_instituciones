package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.ClassroomStudent;
import backend_instituciones.backend_instituciones.domain.entity.User;
import backend_instituciones.backend_instituciones.domain.enums.Role;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmailAndInstitutionId(String email, Long institutionId);
    Page<User> findByInstitutionId(Long institutionId, Pageable pageable);
    boolean existsByEmailAndInstitutionId(String email, Long institutionId);
    long countByInstitutionIdAndRole(Long institutionId, Role role);
    Optional<User> findByDocumentNumberAndInstitutionId(String documentNumber, Long institutionId);
    List<User> findByInstitutionIdAndRole(Long institutionId, Role role);
    Optional<User> findBySupabaseUid(String supabaseUid);

    @Query("SELECT u FROM User u WHERE u.institutionId = :institutionId " +
           "AND (:role IS NULL OR u.role = :role) " +
           "AND (:active IS NULL OR u.isActive = :active) " +
           "AND (:docNumber IS NULL OR u.documentNumber = :docNumber) " +
           "AND (:q IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "     OR LOWER(u.email) LIKE LOWER(CONCAT('%', :q, '%')) " +
           "     OR LOWER(COALESCE(u.documentNumber,'')) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<User> search(@Param("institutionId") Long institutionId,
                      @Param("role") Role role,
                      @Param("active") Boolean active,
                      @Param("docNumber") String documentNumber,
                      @Param("q") String q,
                      Pageable pageable);

    @Query("SELECT u FROM User u WHERE u.id IN " +
           "(SELECT cs.studentId FROM ClassroomStudent cs WHERE cs.classroomId = :classroomId) " +
           "AND u.institutionId = :institutionId " +
           "AND (:role IS NULL OR u.role = :role) " +
           "AND (:q IS NULL OR LOWER(u.name) LIKE LOWER(CONCAT('%', :q, '%')))")
    Page<User> searchByClassroom(@Param("institutionId") Long institutionId,
                                  @Param("classroomId") Long classroomId,
                                  @Param("role") Role role,
                                  @Param("q") String q,
                                  Pageable pageable);
}
