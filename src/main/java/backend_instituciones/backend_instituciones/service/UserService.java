package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.*;
import backend_instituciones.backend_instituciones.domain.enums.Role;
import backend_instituciones.backend_instituciones.dto.request.CreateUserRequest;
import backend_instituciones.backend_instituciones.dto.request.UpdateUserRequest;
import backend_instituciones.backend_instituciones.dto.response.PageResponse;
import backend_instituciones.backend_instituciones.dto.response.UserResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final UserActivityLogRepository activityLogRepository;
    private final PasswordEncoder passwordEncoder;
    private final SupabaseStorageService storageService;
    private final SupabaseAdminService supabaseAdminService;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final DirectorProfileRepository directorProfileRepository;
    private final GuardianProfileRepository guardianProfileRepository;
    private final AdminProfileRepository adminProfileRepository;
    private final UserRelationsService userRelationsService;

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> list(Long institutionId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(userRepository.findByInstitutionId(institutionId, pageable)
                .map(this::toResponse));
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> search(Long institutionId, Role role, Boolean active,
                                             String q, String documentNumber, Long classroomId,
                                             int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        String qp = (q != null && !q.isBlank()) ? q.trim() : null;
        String docNum = (documentNumber != null && !documentNumber.isBlank()) ? documentNumber.trim() : null;
        if (classroomId != null) {
            return PageResponse.from(userRepository.searchByClassroom(institutionId, classroomId, role, qp, pageable)
                    .map(this::toResponse));
        }
        return PageResponse.from(userRepository.search(institutionId, role, active, docNum, qp, pageable)
                .map(this::toResponse));
    }

    @Transactional
    public UserResponse uploadPhoto(Long id, Long institutionId, MultipartFile file) {
        User user = findOrThrow(id, institutionId);
        String url = storageService.upload(file, "avatars");
        user.setPhotoUrl(url);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse resetPassword(Long id, Long institutionId, String newPassword) {
        User user = findOrThrow(id, institutionId);
        if (newPassword == null || newPassword.isBlank()) {
            newPassword = java.util.UUID.randomUUID().toString().substring(0, 12);
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(true);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse deletePhoto(Long id, Long institutionId) {
        User user = findOrThrow(id, institutionId);
        user.setPhotoUrl(null);
        return toResponse(userRepository.save(user));
    }

    @Transactional(readOnly = true)
    public UserResponse get(Long id, Long institutionId) {
        return toResponse(findOrThrow(id, institutionId));
    }

    @Transactional
    public UserResponse create(Long institutionId, CreateUserRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmailAndInstitutionId(email, institutionId)) {
            throw new BusinessException("Email already in use", HttpStatus.CONFLICT, "EMAIL_TAKEN");
        }

        // ADMINISTRACION solo puede crear DOCENTE, ESTUDIANTE, ALMACEN
        if (callerHasRole("ROLE_ADMINISTRACION")) {
            Set<Role> allowed = Set.of(Role.DOCENTE, Role.ESTUDIANTE, Role.ALMACEN);
            if (!allowed.contains(request.getRole())) {
                throw new BusinessException("Este rol no puede crear usuarios de ese tipo",
                        HttpStatus.FORBIDDEN, "FORBIDDEN");
            }
        }

        // Create user in Supabase Auth — get UUID back
        // Same email can exist in another institution: reuse their supabase_uid (Supabase Auth is global)
        String supabaseUid = null;
        try {
            supabaseUid = supabaseAdminService.createAuthUser(email, request.getPassword());
        } catch (Exception e) {
            if ("EMAIL_ALREADY_REGISTERED".equals(e.getMessage())) {
                // Reuse existing supabase_uid from another institution
                supabaseUid = userRepository.findFirstByEmail(email)
                        .map(User::getSupabaseUid)
                        .orElse(null);
                if (supabaseUid == null) {
                    // Email exists in Supabase but not in our DB at all — can't recover
                    throw new BusinessException(
                        "El correo ya está registrado en Supabase pero no en el sistema. Contacta soporte.",
                        HttpStatus.CONFLICT, "EMAIL_TAKEN_EXTERNAL");
                }
                // supabaseUid found — continue normally, same person joining another institution
            } else {
                throw new BusinessException(
                    "Failed to create auth user: " + e.getMessage(),
                    HttpStatus.INTERNAL_SERVER_ERROR,
                    "SUPABASE_ERROR"
                );
            }
        }

        User user = User.builder()
                .institutionId(institutionId)
                .name(request.getName())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .supabaseUid(supabaseUid)
                .role(request.getRole())
                .isActive(true)
                .mustCompleteProfile(false)
                .mustChangePassword(true)
                .phone(blankToNull(request.getPhone()))
                .alternativePhone(blankToNull(request.getAlternativePhone()))
                .documentType(blankToNull(request.getDocumentType()))
                .documentNumber(blankToNull(request.getDocumentNumber()))
                .birthDate(request.getBirthDate())
                .gender(blankToNull(request.getGender()))
                .address(blankToNull(request.getAddress()))
                .district(blankToNull(request.getDistrict()))
                .city(blankToNull(request.getCity()))
                .build();

        user = userRepository.save(user);
        createProfileWithData(user, request);

        if (request.getRole() == Role.ESTUDIANTE) {
            userRelationsService.autoAssignSectionIfPresent(user.getId(), institutionId,
                    request.getLevelId(), request.getGradeId(), request.getSectionId());
        } else if (request.getRole() == Role.PADRE) {
            userRelationsService.autoLinkStudentsIfPresent(user.getId(), institutionId,
                    request.getLinkedStudentIds());
        }

        return toResponse(user);
    }

    @Transactional
    public UserResponse update(Long id, Long institutionId, UpdateUserRequest request) {
        User user = findOrThrow(id, institutionId);
        if (request.getName() != null && !request.getName().isBlank()) {
            user.setName(request.getName());
        }
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        userRepository.save(user);

        // Persist adminPermissions for ADMINISTRACION (normalize before write)
        if (request.getAdminPermissions() != null && user.getRole() == Role.ADMINISTRACION) {
            Map<String, Map<String, Boolean>> normalized = normalizeAdminPermissions(request.getAdminPermissions());
            adminProfileRepository.findByUserId(id).ifPresent(p -> {
                p.setAdminPermissions(normalized);
                adminProfileRepository.save(p);
            });
        }

        return toResponse(user);
    }

    @Transactional
    public void delete(Long id, Long institutionId) {
        User user = findOrThrow(id, institutionId);
        deleteProfile(user);
        userRepository.delete(user);
    }

    @Transactional
    public UserResponse changeRole(Long id, Long institutionId, Role role) {
        User user = findOrThrow(id, institutionId);
        if (user.getRole() != role) {
            deleteProfile(user);
            user.setRole(role);
            user = userRepository.save(user);
            createEmptyProfile(user);
        }
        return toResponse(user);
    }

    @Transactional
    public UserResponse activate(Long id, Long institutionId) {
        User user = findOrThrow(id, institutionId);
        user.setActive(true);
        return toResponse(userRepository.save(user));
    }

    @Transactional
    public UserResponse deactivate(Long id, Long institutionId) {
        User user = findOrThrow(id, institutionId);
        user.setActive(false);
        return toResponse(userRepository.save(user));
    }

    public PageResponse<?> getActivity(Long id, Long institutionId, int page, int size) {
        findOrThrow(id, institutionId);
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(activityLogRepository
                .findByUserIdAndInstitutionIdOrderByCreatedAtDesc(id, institutionId, pageable));
    }

    public void logActivity(Long userId, Long institutionId, String action, String details) {
        activityLogRepository.save(UserActivityLog.builder()
                .userId(userId)
                .institutionId(institutionId)
                .action(action)
                .details(details)
                .build());
    }

    public User findOrThrow(Long id, Long institutionId) {
        return userRepository.findById(id)
                .filter(u -> u.getInstitutionId().equals(institutionId))
                .orElseThrow(() -> new ResourceNotFoundException("User", id));
    }

    public UserResponse toResponse(User u) {
        String firstName = u.getFirstName();
        String lastName = u.getLastName();
        if (firstName == null && u.getName() != null) {
            String[] parts = u.getName().split(" ", 2);
            firstName = parts[0];
            lastName = parts.length > 1 ? parts[1] : "";
        }

        UserResponse.UserResponseBuilder b = UserResponse.builder()
                .id(u.getId())
                .institutionId(u.getInstitutionId())
                .name(u.getName())
                .firstName(firstName)
                .lastName(lastName)
                .email(u.getEmail())
                .phone(u.getPhone())
                .alternativePhone(u.getAlternativePhone())
                .documentType(u.getDocumentType())
                .documentNumber(u.getDocumentNumber())
                .birthDate(u.getBirthDate())
                .gender(u.getGender())
                .address(u.getAddress())
                .district(u.getDistrict())
                .city(u.getCity())
                .photoUrl(u.getPhotoUrl())
                .role(u.getRole())
                .status(u.isActive() ? "ACTIVE" : "INACTIVE")
                .active(u.isActive())
                .mustCompleteProfile(Boolean.TRUE.equals(u.getMustCompleteProfile()))
                .mustChangePassword(Boolean.TRUE.equals(u.getMustChangePassword()))
                .createdAt(u.getCreatedAt())
                .updatedAt(u.getUpdatedAt());

        if (u.getId() != null) {
            applyRoleProfile(b, u);
        }

        return b.build();
    }

    private void applyRoleProfile(UserResponse.UserResponseBuilder b, User u) {
        if (u.getRole() == null) return;
        switch (u.getRole()) {
            case ESTUDIANTE -> studentProfileRepository.findByUserId(u.getId()).ifPresent(p ->
                    b.studentCode(p.getStudentCode())
                     .admissionDate(p.getAdmissionDate())
                     .bloodType(p.getBloodType())
                     .allergies(p.getAllergies())
                     .medicalNotes(p.getMedicalNotes())
                     .specialNeeds(p.getSpecialNeeds())
                     .emergencyPhone(p.getEmergencyPhone()));
            case DOCENTE -> teacherProfileRepository.findByUserId(u.getId()).ifPresent(p ->
                    b.employeeCode(p.getEmployeeCode())
                     .specialty(p.getSpecialty())
                     .hireDate(p.getHireDate())
                     .professionalLicense(p.getProfessionalLicense()));
            case DIRECTOR -> directorProfileRepository.findByUserId(u.getId()).ifPresent(p ->
                    b.employeeCode(p.getEmployeeCode())
                     .hireDate(p.getHireDate())
                     .position(p.getPosition()));
            case PADRE -> guardianProfileRepository.findByUserId(u.getId()).ifPresent(p ->
                    b.occupation(p.getOccupation())
                     .workplace(p.getWorkplace())
                     .billingEmail(p.getBillingEmail()));
            case ADMIN -> adminProfileRepository.findByUserId(u.getId()).ifPresent(p ->
                    b.employeeCode(p.getEmployeeCode())
                     .hireDate(p.getHireDate())
                     .position(p.getPosition()));
            case ALMACEN -> adminProfileRepository.findByUserId(u.getId()).ifPresent(p ->
                    b.employeeCode(p.getEmployeeCode())
                     .hireDate(p.getHireDate())
                     .position(p.getPosition()));
            case ADMINISTRACION -> adminProfileRepository.findByUserId(u.getId()).ifPresent(p ->
                    b.employeeCode(p.getEmployeeCode())
                     .hireDate(p.getHireDate())
                     .position(p.getPosition())
                     .adminPermissions(normalizeAdminPermissions(p.getAdminPermissions())));
            default -> {}
        }
    }

    private static final List<String> ADMIN_MODULES =
            List.of("USUARIOS", "ESTRUCTURA", "PAGOS", "ANUNCIOS", "ASISTENCIA", "ALMACEN");

    private Map<String, Map<String, Boolean>> normalizeAdminPermissions(
            Map<String, Map<String, Boolean>> raw) {
        Map<String, Map<String, Boolean>> result = new java.util.LinkedHashMap<>();
        for (String module : ADMIN_MODULES) {
            boolean access = false;
            if (raw != null) {
                Map<String, Boolean> modulePerms = raw.get(module);
                if (modulePerms != null) {
                    // prefer explicit 'access', fall back to legacy 'view' or 'manage'
                    if (modulePerms.containsKey("access")) {
                        access = Boolean.TRUE.equals(modulePerms.get("access"));
                    } else if (Boolean.TRUE.equals(modulePerms.get("view"))
                            || Boolean.TRUE.equals(modulePerms.get("manage"))) {
                        access = true;
                    }
                }
            }
            result.put(module, Map.of("access", access));
        }
        return result;
    }

    private void createEmptyProfile(User user) {
        if (user.getRole() == null || user.getId() == null) return;
        switch (user.getRole()) {
            case ESTUDIANTE -> studentProfileRepository.save(StudentProfile.builder().userId(user.getId()).build());
            case DOCENTE -> teacherProfileRepository.save(TeacherProfile.builder().userId(user.getId()).build());
            case DIRECTOR -> directorProfileRepository.save(DirectorProfile.builder().userId(user.getId()).build());
            case PADRE -> guardianProfileRepository.save(GuardianProfile.builder().userId(user.getId()).build());
            case ADMIN -> adminProfileRepository.save(AdminProfile.builder().userId(user.getId()).build());
            case ALMACEN -> adminProfileRepository.save(AdminProfile.builder().userId(user.getId()).build());
            case ADMINISTRACION -> adminProfileRepository.save(AdminProfile.builder().userId(user.getId()).build());
            default -> {}
        }
    }

    private void createProfileWithData(User user, CreateUserRequest request) {
        if (user.getRole() == null || user.getId() == null) return;
        switch (user.getRole()) {
            case ESTUDIANTE -> studentProfileRepository.save(StudentProfile.builder()
                    .userId(user.getId())
                    .admissionDate(request.getAdmissionDate())
                    .bloodType(blankToNull(request.getBloodType()))
                    .allergies(blankToNull(request.getAllergies()))
                    .medicalNotes(blankToNull(request.getMedicalNotes()))
                    .specialNeeds(blankToNull(request.getSpecialNeeds()))
                    .emergencyPhone(blankToNull(request.getEmergencyPhone()))
                    .build());
            case DOCENTE -> teacherProfileRepository.save(TeacherProfile.builder()
                    .userId(user.getId())
                    .specialty(blankToNull(request.getSpecialty()))
                    .hireDate(request.getHireDate())
                    .professionalLicense(blankToNull(request.getProfessionalLicense()))
                    .build());
            case DIRECTOR -> directorProfileRepository.save(DirectorProfile.builder()
                    .userId(user.getId())
                    .hireDate(request.getHireDate())
                    .position(blankToNull(request.getPosition()))
                    .build());
            case PADRE -> guardianProfileRepository.save(GuardianProfile.builder()
                    .userId(user.getId())
                    .occupation(blankToNull(request.getOccupation()))
                    .workplace(blankToNull(request.getWorkplace()))
                    .billingEmail(blankToNull(request.getBillingEmail()))
                    .build());
            case ADMIN -> adminProfileRepository.save(AdminProfile.builder().userId(user.getId()).build());
            case ALMACEN -> adminProfileRepository.save(AdminProfile.builder()
                    .userId(user.getId())
                    .hireDate(request.getHireDate())
                    .position(blankToNull(request.getPosition()))
                    .build());
            case ADMINISTRACION -> adminProfileRepository.save(AdminProfile.builder()
                    .userId(user.getId())
                    .adminPermissions(normalizeAdminPermissions(request.getAdminPermissions()))
                    .build());
            default -> {}
        }
    }

    private void deleteProfile(User user) {
        if (user.getRole() == null || user.getId() == null) return;
        switch (user.getRole()) {
            case ESTUDIANTE -> studentProfileRepository.deleteById(user.getId());
            case DOCENTE -> teacherProfileRepository.deleteById(user.getId());
            case DIRECTOR -> directorProfileRepository.deleteById(user.getId());
            case PADRE -> guardianProfileRepository.deleteById(user.getId());
            case ADMIN -> adminProfileRepository.deleteById(user.getId());
            case ALMACEN -> adminProfileRepository.deleteById(user.getId());
            case ADMINISTRACION -> adminProfileRepository.deleteById(user.getId());
            default -> {}
        }
    }

    private boolean callerHasRole(String role) {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        return auth.getAuthorities().stream().anyMatch(a -> role.equals(a.getAuthority()));
    }

    private String blankToNull(String value) {
        return (value == null || value.isBlank()) ? null : value.trim();
    }
}
