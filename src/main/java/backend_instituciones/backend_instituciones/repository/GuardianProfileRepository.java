package backend_instituciones.backend_instituciones.repository;

import backend_instituciones.backend_instituciones.domain.entity.GuardianProfile;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface GuardianProfileRepository extends JpaRepository<GuardianProfile, Long> {
    Optional<GuardianProfile> findByUserId(Long userId);
}
