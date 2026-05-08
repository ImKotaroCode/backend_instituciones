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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

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

    public PageResponse<UserResponse> list(Long institutionId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(userRepository.findByInstitutionId(institutionId, pageable)
                .map(this::toResponse));
    }

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

    public UserResponse get(Long id, Long institutionId) {
        return toResponse(findOrThrow(id, institutionId));
    }

    @Transactional
    public UserResponse create(Long institutionId, CreateUserRequest request) {
        String email = request.getEmail().toLowerCase().trim();

        if (userRepository.existsByEmailAndInstitutionId(email, institutionId)) {
            throw new BusinessException("Email already in use", HttpStatus.CONFLICT, "EMAIL_TAKEN");
        }

        // Create user in Supabase Auth — get UUID back
        String supabaseUid = null;
        try {
            supabaseUid = supabaseAdminService.createAuthUser(email, request.getPassword());
        } catch (Exception e) {
            throw new BusinessException(
                "Failed to create auth user: " + e.getMessage(),
                HttpStatus.INTERNAL_SERVER_ERROR,
                "SUPABASE_ERROR"
            );
        }

        User user = User.builder()
                .institutionId(institutionId)
                .name(request.getName())
                .email(email)
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .supabaseUid(supabaseUid)
                .role(request.getRole())
                .isActive(true)
                .mustCompleteProfile(true)
                .mustChangePassword(true)
                .build();

        user = userRepository.save(user);
        createEmptyProfile(user);
        return toResponse(user);
    }

    @Transactional
    public UserResponse update(Long id, Long institutionId, UpdateUserRequest request) {
        User user = findOrThrow(id, institutionId);
        user.setName(request.getName());
        if (request.getPassword() != null && !request.getPassword().isBlank()) {
            user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        }
        return toResponse(userRepository.save(user));
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
            default -> {}
        }
    }

    private void createEmptyProfile(User user) {
        if (user.getRole() == null || user.getId() == null) return;
        switch (user.getRole()) {
            case ESTUDIANTE -> studentProfileRepository.save(StudentProfile.builder().userId(user.getId()).build());
            case DOCENTE -> teacherProfileRepository.save(TeacherProfile.builder().userId(user.getId()).build());
            case DIRECTOR -> directorProfileRepository.save(DirectorProfile.builder().userId(user.getId()).build());
            case PADRE -> guardianProfileRepository.save(GuardianProfile.builder().userId(user.getId()).build());
            case ADMIN -> adminProfileRepository.save(AdminProfile.builder().userId(user.getId()).build());
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
            default -> {}
        }
    }
}
