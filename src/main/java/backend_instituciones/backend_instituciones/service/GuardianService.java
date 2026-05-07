package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.StudentGuardian;
import backend_instituciones.backend_instituciones.domain.entity.User;
import backend_instituciones.backend_instituciones.dto.request.GuardianRequest;
import backend_instituciones.backend_instituciones.dto.response.GuardianResponse;
import backend_instituciones.backend_instituciones.dto.response.UserResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.StudentGuardianRepository;
import backend_instituciones.backend_instituciones.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GuardianService {

    private final StudentGuardianRepository guardianRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    public List<GuardianResponse> listGuardians(Long studentId, Long institutionId) {
        requireUser(studentId, institutionId);
        return guardianRepository.findByStudentIdAndInstitutionId(studentId, institutionId)
                .stream().map(this::toGuardianResponse).toList();
    }

    @Transactional
    public GuardianResponse addGuardian(Long studentId, Long institutionId, GuardianRequest request) {
        requireUser(studentId, institutionId);
        requireUser(request.getGuardianId(), institutionId);
        if (guardianRepository.existsByStudentIdAndGuardianId(studentId, request.getGuardianId())) {
            throw new BusinessException("Guardian already linked to this student", HttpStatus.CONFLICT, "ALREADY_LINKED");
        }
        StudentGuardian entity = StudentGuardian.builder()
                .institutionId(institutionId)
                .studentId(studentId)
                .guardianId(request.getGuardianId())
                .relationship(request.getRelationship())
                .isPrimaryContact(request.isPrimaryContact())
                .isBillingContact(request.isBillingContact())
                .isEmergencyContact(request.isEmergencyContact())
                .livesWithStudent(request.isLivesWithStudent())
                .build();
        return toGuardianResponse(guardianRepository.save(entity));
    }

    @Transactional
    public void removeGuardian(Long studentId, Long guardianId, Long institutionId) {
        requireUser(studentId, institutionId);
        guardianRepository.deleteByStudentIdAndGuardianId(studentId, guardianId);
    }

    @Transactional
    public GuardianResponse updateRelationship(Long studentId, Long guardianId, Long institutionId,
                                               backend_instituciones.backend_instituciones.dto.request.GuardianRequest request) {
        StudentGuardian sg = guardianRepository.findByStudentIdAndGuardianId(studentId, guardianId)
                .orElseThrow(() -> new ResourceNotFoundException("Guardian link", guardianId));
        sg.setRelationship(request.getRelationship());
        sg.setIsPrimaryContact(request.isPrimaryContact());
        sg.setIsBillingContact(request.isBillingContact());
        sg.setIsEmergencyContact(request.isEmergencyContact());
        sg.setLivesWithStudent(request.isLivesWithStudent());
        return toGuardianResponse(guardianRepository.save(sg));
    }

    public List<UserResponse> listStudentsOfGuardian(Long guardianId, Long institutionId) {
        requireUser(guardianId, institutionId);
        return guardianRepository.findByGuardianIdAndInstitutionId(guardianId, institutionId)
                .stream()
                .map(sg -> userRepository.findById(sg.getStudentId())
                        .map(userService::toResponse).orElse(null))
                .filter(u -> u != null)
                .toList();
    }

    private User requireUser(Long userId, Long institutionId) {
        return userRepository.findById(userId)
                .filter(u -> u.getInstitutionId().equals(institutionId))
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));
    }

    private GuardianResponse toGuardianResponse(StudentGuardian sg) {
        User guardian = userRepository.findById(sg.getGuardianId()).orElse(null);
        return GuardianResponse.builder()
                .id(sg.getId())
                .studentId(sg.getStudentId())
                .guardianId(sg.getGuardianId())
                .guardianName(guardian != null ? guardian.getName() : null)
                .guardianEmail(guardian != null ? guardian.getEmail() : null)
                .guardianPhotoUrl(guardian != null ? guardian.getPhotoUrl() : null)
                .relationship(sg.getRelationship())
                .isPrimaryContact(Boolean.TRUE.equals(sg.getIsPrimaryContact()))
                .isBillingContact(Boolean.TRUE.equals(sg.getIsBillingContact()))
                .isEmergencyContact(Boolean.TRUE.equals(sg.getIsEmergencyContact()))
                .livesWithStudent(Boolean.TRUE.equals(sg.getLivesWithStudent()))
                .createdAt(sg.getCreatedAt())
                .build();
    }
}
