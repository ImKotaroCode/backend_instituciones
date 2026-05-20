package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.*;
import backend_instituciones.backend_instituciones.dto.request.DependentsRequest;
import backend_instituciones.backend_instituciones.dto.request.StudentSectionRequest;
import backend_instituciones.backend_instituciones.dto.response.CourseAssignmentResponse;
import backend_instituciones.backend_instituciones.dto.response.DependentsResponse;
import backend_instituciones.backend_instituciones.dto.response.StudentSectionResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
public class UserRelationsService {

    private final StudentSectionAssignmentRepository studentSectionRepo;
    private final ParentStudentLinkRepository parentStudentLinkRepo;
    private final UserRepository userRepository;
    private final AcademicLevelRepository levelRepository;
    private final AcademicGradeRepository gradeRepository;
    private final AcademicSectionRepository sectionRepository;
    private final ClassroomRepository classroomRepository;
    private final CourseAssignmentRepository courseAssignmentRepository;
    private final CourseAssignmentService courseAssignmentService;

    // ── GET /users/{userId}/student-section ────────────────────────────────────

    @Transactional(readOnly = true)
    public StudentSectionResponse getStudentSection(Long userId, Long institutionId) {
        userRepository.findById(userId)
                .filter(u -> u.getInstitutionId().equals(institutionId))
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        return studentSectionRepo.findByInstitutionIdAndStudentId(institutionId, userId)
                .map(a -> {
                    String levelName = levelRepository.findById(a.getLevelId()).map(l -> l.getName()).orElse(null);
                    String gradeName = gradeRepository.findById(a.getGradeId()).map(g -> g.getName()).orElse(null);
                    String sectionName = sectionRepository.findById(a.getSectionId()).map(s -> s.getName()).orElse(null);
                    return StudentSectionResponse.builder()
                            .userId(userId)
                            .levelId(a.getLevelId())
                            .gradeId(a.getGradeId())
                            .sectionId(a.getSectionId())
                            .levelName(levelName)
                            .gradeName(gradeName)
                            .sectionName(sectionName)
                            .build();
                })
                .orElse(null);
    }

    // ── PUT /users/{userId}/student-section ────────────────────────────────────

    public StudentSectionResponse assignStudentSection(Long userId, Long institutionId,
                                                        StudentSectionRequest req) {
        userRepository.findById(userId)
                .filter(u -> u.getInstitutionId().equals(institutionId))
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Validate hierarchy
        AcademicLevel level = levelRepository.findByIdAndInstitutionId(req.getLevelId(), institutionId)
                .orElseThrow(() -> new BusinessException("Level not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));

        AcademicGrade grade = gradeRepository.findByIdAndLevelId(req.getGradeId(), level.getId())
                .orElseThrow(() -> new BusinessException("Grade does not belong to level",
                        HttpStatus.BAD_REQUEST, "VALIDATION_ERROR"));

        AcademicSection section = sectionRepository.findByIdAndGradeId(req.getSectionId(), grade.getId())
                .orElseThrow(() -> new BusinessException("Section does not belong to grade",
                        HttpStatus.BAD_REQUEST, "VALIDATION_ERROR"));

        StudentSectionAssignment assignment = studentSectionRepo
                .findByInstitutionIdAndStudentId(institutionId, userId)
                .orElseGet(() -> StudentSectionAssignment.builder()
                        .institutionId(institutionId)
                        .studentId(userId)
                        .build());

        assignment.setLevelId(level.getId());
        assignment.setGradeId(grade.getId());
        assignment.setSectionId(section.getId());
        studentSectionRepo.save(assignment);

        return StudentSectionResponse.builder()
                .userId(userId)
                .levelId(level.getId())
                .gradeId(grade.getId())
                .sectionId(section.getId())
                .levelName(level.getName())
                .gradeName(grade.getName())
                .sectionName(section.getName())
                .build();
    }

    // ── GET /users/{userId}/dependents ─────────────────────────────────────────

    @Transactional(readOnly = true)
    public DependentsResponse getDependents(Long userId, Long institutionId) {
        userRepository.findById(userId)
                .filter(u -> u.getInstitutionId().equals(institutionId))
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        List<ParentStudentLink> links = parentStudentLinkRepo.findByInstitutionIdAndParentId(institutionId, userId);
        List<Long> studentIds = links.stream().map(ParentStudentLink::getStudentId).toList();

        List<User> students = studentIds.isEmpty()
                ? List.of()
                : userRepository.findAllById(studentIds).stream()
                        .filter(u -> u.getInstitutionId().equals(institutionId))
                        .toList();

        return DependentsResponse.builder()
                .userId(userId)
                .studentIds(studentIds)
                .students(students.stream().map(s -> DependentsResponse.StudentInfo.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .documentNumber(s.getDocumentNumber())
                        .build()).toList())
                .build();
    }

    // ── PUT /users/{userId}/dependents ─────────────────────────────────────────

    public DependentsResponse assignDependents(Long userId, Long institutionId,
                                                DependentsRequest req) {
        userRepository.findById(userId)
                .filter(u -> u.getInstitutionId().equals(institutionId))
                .orElseThrow(() -> new ResourceNotFoundException("User", userId));

        // Replace all links
        parentStudentLinkRepo.deleteByInstitutionIdAndParentId(institutionId, userId);

        for (Long studentId : req.getStudentIds()) {
            userRepository.findById(studentId)
                    .filter(u -> u.getInstitutionId().equals(institutionId))
                    .orElseThrow(() -> new BusinessException(
                            "Student " + studentId + " not found", HttpStatus.NOT_FOUND, "NOT_FOUND"));

            // Skip if already exists (idempotent guard)
            if (!parentStudentLinkRepo.existsByInstitutionIdAndParentIdAndStudentId(
                    institutionId, userId, studentId)) {
                parentStudentLinkRepo.save(ParentStudentLink.builder()
                        .institutionId(institutionId)
                        .parentId(userId)
                        .studentId(studentId)
                        .build());
            }
        }

        List<User> students = userRepository.findAllById(req.getStudentIds()).stream()
                .filter(u -> u.getInstitutionId().equals(institutionId))
                .toList();

        return DependentsResponse.builder()
                .userId(userId)
                .studentIds(req.getStudentIds())
                .students(students.stream().map(s -> DependentsResponse.StudentInfo.builder()
                        .id(s.getId())
                        .name(s.getName())
                        .documentNumber(s.getDocumentNumber())
                        .build()).toList())
                .build();
    }

    // ── GET /students/{studentId}/courses ──────────────────────────────────────

    @Transactional(readOnly = true)
    public List<CourseAssignmentResponse> getStudentCourses(Long studentId, Long institutionId) {
        StudentSectionAssignment assignment = studentSectionRepo
                .findByInstitutionIdAndStudentId(institutionId, studentId)
                .orElse(null);

        if (assignment == null) return List.of();

        List<Classroom> classrooms = classroomRepository
                .findByInstitutionIdAndAcademicLevelIdAndAcademicGradeIdAndAcademicSectionId(
                        institutionId,
                        assignment.getLevelId(),
                        assignment.getGradeId(),
                        assignment.getSectionId());

        if (classrooms.isEmpty()) return List.of();

        List<Long> classroomIds = classrooms.stream().map(Classroom::getId).toList();
        List<CourseAssignment> courseAssignments =
                courseAssignmentRepository.findByClassroomIdInAndInstitutionId(classroomIds, institutionId);

        return courseAssignmentService.toBatchResponsesPublic(courseAssignments);
    }

    // ── Auto-assign on user creation ───────────────────────────────────────────

    public void autoAssignSectionIfPresent(Long userId, Long institutionId,
                                            Long levelId, Long gradeId, Long sectionId) {
        if (levelId == null || gradeId == null || sectionId == null) return;
        assignStudentSection(userId, institutionId,
                buildSectionRequest(levelId, gradeId, sectionId));
    }

    public void autoLinkStudentsIfPresent(Long parentId, Long institutionId,
                                           List<Long> studentIds) {
        if (studentIds == null || studentIds.isEmpty()) return;
        DependentsRequest req = new DependentsRequest();
        req.setStudentIds(studentIds);
        assignDependents(parentId, institutionId, req);
    }

    private StudentSectionRequest buildSectionRequest(Long levelId, Long gradeId, Long sectionId) {
        StudentSectionRequest r = new StudentSectionRequest();
        r.setLevelId(levelId);
        r.setGradeId(gradeId);
        r.setSectionId(sectionId);
        return r;
    }
}
