package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.*;
import backend_instituciones.backend_instituciones.dto.request.AcademicStatusRequest;
import backend_instituciones.backend_instituciones.dto.request.EnrollmentRequest;
import backend_instituciones.backend_instituciones.dto.response.AcademicStatusResponse;
import backend_instituciones.backend_instituciones.dto.response.EnrollmentResponse;
import backend_instituciones.backend_instituciones.dto.response.PageResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class EnrollmentService {

    private final StudentEnrollmentRepository enrollmentRepository;
    private final StudentAcademicStatusRepository statusRepository;
    private final AcademicYearRepository academicYearRepository;
    private final ClassroomRepository classroomRepository;
    private final UserRepository userRepository;

    public PageResponse<EnrollmentResponse> list(Long institutionId, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("createdAt").descending());
        return PageResponse.from(enrollmentRepository.findByInstitutionId(institutionId, pageable)
                .map(this::toResponse));
    }

    public EnrollmentResponse get(Long id, Long institutionId) {
        return toResponse(findOrThrow(id, institutionId));
    }

    @Transactional
    public EnrollmentResponse create(Long institutionId, EnrollmentRequest request) {
        if (enrollmentRepository.existsByStudentIdAndAcademicYearId(request.getStudentId(), request.getAcademicYearId())) {
            throw new BusinessException("Student already enrolled in this academic year", HttpStatus.CONFLICT, "ALREADY_ENROLLED");
        }
        StudentEnrollment entity = StudentEnrollment.builder()
                .institutionId(institutionId)
                .studentId(request.getStudentId())
                .classroomId(request.getClassroomId())
                .academicYearId(request.getAcademicYearId())
                .enrollmentDate(request.getEnrollmentDate() != null ? request.getEnrollmentDate() : LocalDate.now())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .build();
        StudentEnrollment saved = enrollmentRepository.save(entity);

        statusRepository.findByStudentIdAndAcademicYearId(request.getStudentId(), request.getAcademicYearId())
                .orElseGet(() -> statusRepository.save(StudentAcademicStatus.builder()
                        .institutionId(institutionId)
                        .studentId(request.getStudentId())
                        .academicYearId(request.getAcademicYearId())
                        .enrollmentId(saved.getId())
                        .status("PENDING")
                        .build()));

        return toResponse(saved);
    }

    @Transactional
    public EnrollmentResponse update(Long id, Long institutionId, EnrollmentRequest request) {
        StudentEnrollment entity = findOrThrow(id, institutionId);
        entity.setClassroomId(request.getClassroomId());
        entity.setAcademicYearId(request.getAcademicYearId());
        if (request.getStatus() != null) entity.setStatus(request.getStatus());
        if (request.getEnrollmentDate() != null) entity.setEnrollmentDate(request.getEnrollmentDate());
        return toResponse(enrollmentRepository.save(entity));
    }

    @Transactional
    public void delete(Long id, Long institutionId) {
        StudentEnrollment entity = findOrThrow(id, institutionId);
        enrollmentRepository.delete(entity);
    }

    public List<AcademicStatusResponse> getAcademicHistory(Long studentId, Long institutionId) {
        return statusRepository.findByStudentIdAndInstitutionIdOrderByAcademicYearIdDesc(studentId, institutionId)
                .stream().map(this::toStatusResponse).toList();
    }

    @Transactional
    public AcademicStatusResponse setAcademicStatus(Long studentId, Long institutionId,
                                                     AcademicStatusRequest request) {
        Long actorId = getCurrentUserId();
        StudentAcademicStatus status = statusRepository
                .findByStudentIdAndAcademicYearId(studentId, request.getAcademicYearId())
                .orElseGet(() -> StudentAcademicStatus.builder()
                        .institutionId(institutionId)
                        .studentId(studentId)
                        .academicYearId(request.getAcademicYearId())
                        .build());
        status.setStatus(request.getStatus());
        status.setObservation(request.getObservation());
        status.setSetByUserId(actorId);
        return toStatusResponse(statusRepository.save(status));
    }

    @Transactional
    public AcademicStatusResponse quickStatus(Long studentId, Long institutionId, String status,
                                               Long academicYearId, String observation) {
        AcademicStatusRequest req = new AcademicStatusRequest();
        req.setAcademicYearId(academicYearId);
        req.setStatus(status);
        req.setObservation(observation);
        return setAcademicStatus(studentId, institutionId, req);
    }

    public Map<String, Object> promotionPreview(Long academicYearId, Long institutionId) {
        long promoted = statusRepository.countByAcademicYearIdAndStatus(institutionId, academicYearId, "PROMOTED");
        long repeating = statusRepository.countByAcademicYearIdAndStatus(institutionId, academicYearId, "REPEATING");
        long graduated = statusRepository.countByAcademicYearIdAndStatus(institutionId, academicYearId, "GRADUATED");
        long withdrawn = statusRepository.countByAcademicYearIdAndStatus(institutionId, academicYearId, "WITHDRAWN");
        long transferred = statusRepository.countByAcademicYearIdAndStatus(institutionId, academicYearId, "TRANSFERRED");
        long total = statusRepository.countByAcademicYearId(institutionId, academicYearId);
        long pending = statusRepository.countByAcademicYearIdAndStatus(institutionId, academicYearId, "PENDING");

        Map<String, Object> preview = new HashMap<>();
        preview.put("academicYearId", academicYearId);
        preview.put("promoted", promoted);
        preview.put("repeating", repeating);
        preview.put("graduated", graduated);
        preview.put("withdrawn", withdrawn);
        preview.put("transferred", transferred);
        preview.put("missingStatus", pending);
        preview.put("total", total);
        preview.put("ready", pending == 0);
        return preview;
    }

    @Transactional
    public Map<String, Object> promotionExecute(Long academicYearId, Long institutionId) {
        academicYearRepository.findByIdAndInstitutionId(academicYearId, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("AcademicYear", academicYearId));

        List<StudentAcademicStatus> promotedStatuses =
                statusRepository.findByAcademicYearIdAndStatus(institutionId, academicYearId, "PROMOTED");

        int created = 0;
        int skipped = 0;
        for (StudentAcademicStatus s : promotedStatuses) {
            StudentEnrollment current = enrollmentRepository
                    .findByStudentIdAndAcademicYearId(s.getStudentId(), academicYearId)
                    .orElse(null);
            if (current == null) { skipped++; continue; }
            created++;
        }

        Map<String, Object> result = new HashMap<>();
        result.put("academicYearId", academicYearId);
        result.put("processed", promotedStatuses.size());
        result.put("enrollmentsCreated", created);
        result.put("skipped", skipped);
        result.put("message", "Promotion recorded. Assign classrooms for next year individually or via import.");
        return result;
    }

    private StudentEnrollment findOrThrow(Long id, Long institutionId) {
        return enrollmentRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Enrollment", id));
    }

    private Long getCurrentUserId() {
        try {
            Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();
            if (principal instanceof org.springframework.security.core.userdetails.UserDetails ud) {
                return Long.valueOf(ud.getUsername());
            }
        } catch (Exception ignored) {}
        return null;
    }

    private EnrollmentResponse toResponse(StudentEnrollment e) {
        String studentName = userRepository.findById(e.getStudentId()).map(User::getName).orElse(null);
        String classroomName = classroomRepository.findById(e.getClassroomId()).map(Classroom::getName).orElse(null);
        String yearName = academicYearRepository.findById(e.getAcademicYearId()).map(AcademicYear::getName).orElse(null);
        return EnrollmentResponse.builder()
                .id(e.getId())
                .institutionId(e.getInstitutionId())
                .studentId(e.getStudentId())
                .studentName(studentName)
                .classroomId(e.getClassroomId())
                .classroomName(classroomName)
                .academicYearId(e.getAcademicYearId())
                .academicYearName(yearName)
                .enrollmentDate(e.getEnrollmentDate())
                .status(e.getStatus())
                .createdAt(e.getCreatedAt())
                .build();
    }

    private AcademicStatusResponse toStatusResponse(StudentAcademicStatus s) {
        String yearName = academicYearRepository.findById(s.getAcademicYearId()).map(AcademicYear::getName).orElse(null);
        String classroomName = null;
        if (s.getEnrollmentId() != null) {
            classroomName = enrollmentRepository.findById(s.getEnrollmentId())
                    .flatMap(e -> classroomRepository.findById(e.getClassroomId()))
                    .map(Classroom::getName).orElse(null);
        }
        return AcademicStatusResponse.builder()
                .id(s.getId())
                .studentId(s.getStudentId())
                .academicYearId(s.getAcademicYearId())
                .academicYearName(yearName)
                .enrollmentId(s.getEnrollmentId())
                .classroomName(classroomName)
                .status(s.getStatus())
                .observation(s.getObservation())
                .createdAt(s.getCreatedAt())
                .build();
    }
}
