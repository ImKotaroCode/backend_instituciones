package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.*;
import backend_instituciones.backend_instituciones.domain.enums.Role;
import backend_instituciones.backend_instituciones.dto.request.ProfileUpdateRequest;
import backend_instituciones.backend_instituciones.dto.response.UserResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final UserRepository userRepository;
    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final SupabaseStorageService storageService;
    private final StudentProfileRepository studentProfileRepository;
    private final TeacherProfileRepository teacherProfileRepository;
    private final DirectorProfileRepository directorProfileRepository;
    private final GuardianProfileRepository guardianProfileRepository;
    private final AdminProfileRepository adminProfileRepository;

    @Transactional(readOnly = true)
    public UserResponse getProfile(Long userId) {
        User user = findUser(userId);
        return userService.toResponse(user);
    }

    @Transactional
    public UserResponse updateProfile(Long userId, ProfileUpdateRequest req) {
        User user = findUser(userId);

        if (req.getFirstName() != null) user.setFirstName(req.getFirstName());
        if (req.getLastName() != null) user.setLastName(req.getLastName());
        if (req.getFirstName() != null || req.getLastName() != null) {
            String fn = req.getFirstName() != null ? req.getFirstName() : (user.getFirstName() != null ? user.getFirstName() : "");
            String ln = req.getLastName() != null ? req.getLastName() : (user.getLastName() != null ? user.getLastName() : "");
            user.setName((fn + " " + ln).trim());
        }
        if (req.getPhone() != null) user.setPhone(req.getPhone());
        if (req.getAlternativePhone() != null) user.setAlternativePhone(req.getAlternativePhone());
        if (req.getDocumentType() != null) user.setDocumentType(req.getDocumentType());
        if (req.getDocumentNumber() != null) user.setDocumentNumber(req.getDocumentNumber());
        if (req.getBirthDate() != null) user.setBirthDate(req.getBirthDate());
        if (req.getGender() != null) user.setGender(req.getGender());
        if (req.getAddress() != null) user.setAddress(req.getAddress());
        if (req.getDistrict() != null) user.setDistrict(req.getDistrict());
        if (req.getCity() != null) user.setCity(req.getCity());

        user = userRepository.save(user);
        updateRoleProfile(user, req);

        return userService.toResponse(user);
    }

    @Transactional
    public UserResponse uploadPhoto(Long userId, MultipartFile file) {
        User user = findUser(userId);
        validateImageFile(file);
        String url = storageService.upload(file, "avatars");
        user.setPhotoUrl(url);
        return userService.toResponse(userRepository.save(user));
    }

    @Transactional
    public void deletePhoto(Long userId) {
        User user = findUser(userId);
        user.setPhotoUrl(null);
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(Long userId, String currentPassword, String newPassword) {
        User user = findUser(userId);
        if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
            throw new BusinessException("Current password is incorrect", HttpStatus.BAD_REQUEST, "WRONG_PASSWORD");
        }
        if (newPassword == null || newPassword.length() < 8) {
            throw new BusinessException("New password must be at least 8 characters", HttpStatus.BAD_REQUEST, "PASSWORD_TOO_SHORT");
        }
        user.setPasswordHash(passwordEncoder.encode(newPassword));
        user.setMustChangePassword(false);
        userRepository.save(user);
    }

    @Transactional
    public UserResponse completeOnboarding(Long userId) {
        User user = findUser(userId);
        user.setMustCompleteProfile(false);
        return userService.toResponse(userRepository.save(user));
    }

    private void updateRoleProfile(User user, ProfileUpdateRequest req) {
        if (user.getRole() == null) return;
        switch (user.getRole()) {
            case ESTUDIANTE -> {
                StudentProfile p = studentProfileRepository.findByUserId(user.getId())
                        .orElse(StudentProfile.builder().userId(user.getId()).build());
                if (req.getStudentCode() != null) p.setStudentCode(req.getStudentCode());
                if (req.getAdmissionDate() != null) p.setAdmissionDate(req.getAdmissionDate());
                if (req.getBloodType() != null) p.setBloodType(req.getBloodType());
                if (req.getAllergies() != null) p.setAllergies(req.getAllergies());
                if (req.getMedicalNotes() != null) p.setMedicalNotes(req.getMedicalNotes());
                if (req.getSpecialNeeds() != null) p.setSpecialNeeds(req.getSpecialNeeds());
                if (req.getEmergencyPhone() != null) p.setEmergencyPhone(req.getEmergencyPhone());
                studentProfileRepository.save(p);
            }
            case DOCENTE -> {
                TeacherProfile p = teacherProfileRepository.findByUserId(user.getId())
                        .orElse(TeacherProfile.builder().userId(user.getId()).build());
                if (req.getEmployeeCode() != null) p.setEmployeeCode(req.getEmployeeCode());
                if (req.getSpecialty() != null) p.setSpecialty(req.getSpecialty());
                if (req.getHireDate() != null) p.setHireDate(req.getHireDate());
                if (req.getProfessionalLicense() != null) p.setProfessionalLicense(req.getProfessionalLicense());
                teacherProfileRepository.save(p);
            }
            case DIRECTOR -> {
                DirectorProfile p = directorProfileRepository.findByUserId(user.getId())
                        .orElse(DirectorProfile.builder().userId(user.getId()).build());
                if (req.getEmployeeCode() != null) p.setEmployeeCode(req.getEmployeeCode());
                if (req.getHireDate() != null) p.setHireDate(req.getHireDate());
                if (req.getPosition() != null) p.setPosition(req.getPosition());
                directorProfileRepository.save(p);
            }
            case PADRE -> {
                GuardianProfile p = guardianProfileRepository.findByUserId(user.getId())
                        .orElse(GuardianProfile.builder().userId(user.getId()).build());
                if (req.getOccupation() != null) p.setOccupation(req.getOccupation());
                if (req.getWorkplace() != null) p.setWorkplace(req.getWorkplace());
                if (req.getBillingEmail() != null) p.setBillingEmail(req.getBillingEmail());
                guardianProfileRepository.save(p);
            }
            case ADMIN -> {
                AdminProfile p = adminProfileRepository.findByUserId(user.getId())
                        .orElse(AdminProfile.builder().userId(user.getId()).build());
                if (req.getEmployeeCode() != null) p.setEmployeeCode(req.getEmployeeCode());
                if (req.getHireDate() != null) p.setHireDate(req.getHireDate());
                if (req.getPosition() != null) p.setPosition(req.getPosition());
                adminProfileRepository.save(p);
            }
            default -> {}
        }
    }

    private User findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("User not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));
    }

    private void validateImageFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException("File is required", HttpStatus.BAD_REQUEST, "FILE_REQUIRED");
        }
        String ct = file.getContentType();
        if (ct == null || (!ct.equals("image/jpeg") && !ct.equals("image/png") && !ct.equals("image/webp"))) {
            throw new BusinessException("Only JPEG, PNG and WebP images are accepted", HttpStatus.BAD_REQUEST, "INVALID_FILE_TYPE");
        }
        if (file.getSize() > 2 * 1024 * 1024) {
            throw new BusinessException("Image must be under 2MB", HttpStatus.BAD_REQUEST, "FILE_TOO_LARGE");
        }
    }
}
