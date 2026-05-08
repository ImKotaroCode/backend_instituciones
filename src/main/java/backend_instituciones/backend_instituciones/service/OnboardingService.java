package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.*;
import backend_instituciones.backend_instituciones.domain.enums.Role;
import backend_instituciones.backend_instituciones.dto.request.OnboardingRequest;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.repository.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class OnboardingService {

    private final UserRepository userRepository;
    private final AdminProfileRepository adminProfileRepository;
    private final SupabaseAdminService supabaseAdminService;
    private final SaaSCoreService saaSCoreService;

    /**
     * Called by Backend Central (or manually) to provision a new institution.
     * Validates apiKey against Central, then creates admin user.
     *
     * X-API-KEY = institution's apiKey (from Central institutions table)
     */
    @Transactional
    public Map<String, Object> onboard(String apiKey, OnboardingRequest request) {
        // Validate apiKey with Central — get institution data
        SaaSCoreService.LicenciaResponse licencia;
        try {
            licencia = saaSCoreService.validateApiKey(apiKey);
        } catch (Exception e) {
            throw new BusinessException(
                "Invalid or inactive institution: " + e.getMessage(),
                HttpStatus.UNAUTHORIZED,
                "INVALID_API_KEY"
            );
        }

        Long institutionId = licencia.getId();
        String institutionName = licencia.getNombre();
        String email = request.getAdminEmail().toLowerCase().trim();

        log.info("Onboarding: institution={} ({}), admin={}", institutionName, institutionId, email);

        // Idempotent: admin already exists → return existing
        if (userRepository.existsByEmailAndInstitutionId(email, institutionId)) {
            log.info("Onboarding: admin already exists for institution {}", institutionId);
            User existing = userRepository.findByEmailAndInstitutionId(email, institutionId)
                    .orElseThrow();
            return buildResult(existing, false);
        }

        // Generate temp password
        String tempPassword = "Tmp@" + UUID.randomUUID().toString().replace("-", "").substring(0, 12);

        // 1. Create in Supabase Auth
        String supabaseUid = supabaseAdminService.createAuthUser(email, tempPassword);
        log.info("Onboarding: Supabase Auth user created — uid={}", supabaseUid);

        // 2. Insert in local DB
        User user = User.builder()
                .institutionId(institutionId)
                .name("Admin " + institutionName)
                .firstName("Admin")
                .lastName(institutionName)
                .email(email)
                .supabaseUid(supabaseUid)
                .role(Role.ADMIN)
                .isActive(true)
                .mustCompleteProfile(false)
                .mustChangePassword(true)
                .build();

        user = userRepository.save(user);
        adminProfileRepository.save(AdminProfile.builder().userId(user.getId()).build());

        log.info("Onboarding: local user created — id={}, institutionId={}", user.getId(), institutionId);

        return buildResult(user, true);
    }

    private Map<String, Object> buildResult(User user, boolean created) {
        return Map.of(
                "status", created ? "CREATED" : "ALREADY_EXISTS",
                "userId", user.getId(),
                "supabaseUid", user.getSupabaseUid() != null ? user.getSupabaseUid() : "",
                "email", user.getEmail(),
                "institutionId", user.getInstitutionId(),
                "role", user.getRole().name()
        );
    }
}
