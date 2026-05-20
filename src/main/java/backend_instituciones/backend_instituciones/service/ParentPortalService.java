package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.*;
import backend_instituciones.backend_instituciones.dto.response.*;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.repository.*;
import backend_instituciones.backend_instituciones.security.TenantContext;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ParentPortalService {

    private final ParentStudentLinkRepository parentStudentLinkRepo;
    private final UserRepository userRepository;
    private final StudentSectionAssignmentRepository sectionAssignmentRepo;
    private final AcademicLevelRepository levelRepository;
    private final AcademicGradeRepository gradeRepository;
    private final AcademicSectionRepository sectionRepository;
    private final AcademicPeriodRepository periodRepository;
    private final CourseAssessmentRepository assessmentRepository;
    private final CourseAssessmentScoreRepository scoreRepository;
    private final UserRelationsService userRelationsService;
    private final CourseTaskService courseTaskService;
    private final CourseAttendanceOverviewService attendanceOverviewService;

    // ── 1. GET /parents/{parentId}/children ───────────────────────────────────

    public List<ParentChildResponse> getChildren(Long parentId, Long institutionId) {
        assertSelfOrAdmin(parentId);

        List<ParentStudentLink> links = parentStudentLinkRepo
                .findByInstitutionIdAndParentId(institutionId, parentId);

        if (links.isEmpty()) return List.of();

        List<Long> studentIds = links.stream().map(ParentStudentLink::getStudentId).toList();

        Map<Long, User> userMap = userRepository.findAllById(studentIds).stream()
                .filter(u -> u.getInstitutionId().equals(institutionId))
                .collect(Collectors.toMap(User::getId, Function.identity()));

        // Batch load section assignments for all students
        List<StudentSectionAssignment> assignments = studentIds.stream()
                .map(sid -> sectionAssignmentRepo.findByInstitutionIdAndStudentId(institutionId, sid).orElse(null))
                .filter(Objects::nonNull)
                .toList();
        Map<Long, StudentSectionAssignment> asgMap = assignments.stream()
                .collect(Collectors.toMap(StudentSectionAssignment::getStudentId, Function.identity()));

        // Batch load level/grade/section names
        Set<Long> levelIds = assignments.stream().map(StudentSectionAssignment::getLevelId).collect(Collectors.toSet());
        Set<Long> gradeIds = assignments.stream().map(StudentSectionAssignment::getGradeId).collect(Collectors.toSet());
        Set<Long> sectionIds = assignments.stream().map(StudentSectionAssignment::getSectionId).collect(Collectors.toSet());

        Map<Long, String> levelNames = levelIds.isEmpty() ? Map.of()
                : levelRepository.findAllById(levelIds).stream()
                        .collect(Collectors.toMap(AcademicLevel::getId, AcademicLevel::getName));
        Map<Long, String> gradeNames = gradeIds.isEmpty() ? Map.of()
                : gradeRepository.findAllById(gradeIds).stream()
                        .collect(Collectors.toMap(AcademicGrade::getId, AcademicGrade::getName));
        Map<Long, String> sectionNames = sectionIds.isEmpty() ? Map.of()
                : sectionRepository.findAllById(sectionIds).stream()
                        .collect(Collectors.toMap(AcademicSection::getId, AcademicSection::getName));

        return studentIds.stream()
                .map(sid -> {
                    User u = userMap.get(sid);
                    if (u == null) return null;
                    StudentSectionAssignment asg = asgMap.get(sid);
                    return ParentChildResponse.builder()
                            .id(u.getId())
                            .name(u.getName())
                            .firstName(u.getFirstName())
                            .lastName(u.getLastName())
                            .photoUrl(u.getPhotoUrl())
                            .documentNumber(u.getDocumentNumber())
                            .studentCode(null)
                            .levelId(asg != null ? asg.getLevelId() : null)
                            .gradeId(asg != null ? asg.getGradeId() : null)
                            .sectionId(asg != null ? asg.getSectionId() : null)
                            .levelName(asg != null ? levelNames.get(asg.getLevelId()) : null)
                            .gradeName(asg != null ? gradeNames.get(asg.getGradeId()) : null)
                            .sectionName(asg != null ? sectionNames.get(asg.getSectionId()) : null)
                            .status(u.isActive() ? "ACTIVE" : "INACTIVE")
                            .build();
                })
                .filter(Objects::nonNull)
                .sorted(Comparator.comparing(ParentChildResponse::getName, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();
    }

    // ── 2. GET /parents/{parentId}/children/{studentId}/courses ───────────────

    public List<CourseAssignmentResponse> getChildCourses(Long parentId, Long studentId,
                                                           Long institutionId,
                                                           boolean includeInactive) {
        assertSelfOrAdmin(parentId);
        assertParentChild(parentId, studentId, institutionId);

        List<CourseAssignmentResponse> courses =
                userRelationsService.getStudentCourses(studentId, institutionId);

        if (!includeInactive) {
            courses = courses.stream()
                    .filter(c -> !"INACTIVE".equalsIgnoreCase(c.getStatus()))
                    .toList();
        }
        return courses;
    }

    // ── 3. GET …/courses/{courseId}/tasks/overview ────────────────────────────

    public StudentCourseTaskOverviewResponse getChildTasksOverview(Long parentId, Long studentId,
                                                                    Long courseId, Long institutionId,
                                                                    Long periodId, String status) {
        assertSelfOrAdmin(parentId);
        assertParentChild(parentId, studentId, institutionId);
        assertChildCourse(studentId, courseId, institutionId);

        return courseTaskService.getCourseTaskOverview(institutionId, studentId, courseId, periodId, status);
    }

    // ── 4. GET …/courses/{courseId}/attendance/overview ───────────────────────

    public StudentAttendanceOverviewResponse getChildAttendanceOverview(Long parentId, Long studentId,
                                                                         Long courseId, Long institutionId,
                                                                         Long periodId, Long sectionId,
                                                                         LocalDate dateFrom, LocalDate dateTo) {
        assertSelfOrAdmin(parentId);
        assertParentChild(parentId, studentId, institutionId);
        assertChildCourse(studentId, courseId, institutionId);

        return attendanceOverviewService.getStudentOverview(
                institutionId, studentId, courseId, periodId, sectionId, dateFrom, dateTo);
    }

    // ── 5. GET …/courses/{courseId}/grades/overview ───────────────────────────

    public ParentGradesOverviewResponse getChildGradesOverview(Long parentId, Long studentId,
                                                                Long courseId, Long institutionId,
                                                                Long periodId) {
        assertSelfOrAdmin(parentId);
        assertParentChild(parentId, studentId, institutionId);
        assertChildCourse(studentId, courseId, institutionId);

        // Resolve period name
        String periodName = null;
        if (periodId != null) {
            periodName = periodRepository.findByIdAndInstitutionId(periodId, institutionId)
                    .map(AcademicPeriod::getName).orElse(null);
        }

        // Load assessments for course (filter by period if given)
        List<CourseAssessment> assessments =
                assessmentRepository.findByCourseIdAndInstitutionId(courseId, institutionId);
        if (periodId != null) {
            final Long pid = periodId;
            assessments = assessments.stream()
                    .filter(a -> pid.equals(a.getPeriodId()))
                    .toList();
        }

        if (assessments.isEmpty()) {
            return ParentGradesOverviewResponse.builder()
                    .studentId(studentId)
                    .courseId(courseId)
                    .periodId(periodId)
                    .periodName(periodName)
                    .items(List.of())
                    .currentAverageOver20(BigDecimal.ZERO)
                    .build();
        }

        List<Long> assessmentIds = assessments.stream().map(CourseAssessment::getId).toList();

        // Batch load scores for this student across all assessments
        Map<Long, CourseAssessmentScore> scoreMap =
                scoreRepository.findByAssessmentIdInAndStudentId(assessmentIds, studentId).stream()
                        .collect(Collectors.toMap(CourseAssessmentScore::getAssessmentId, Function.identity()));

        BigDecimal totalContribution = BigDecimal.ZERO;

        List<ParentGradesOverviewResponse.GradeItem> items = new ArrayList<>();
        for (CourseAssessment a : assessments) {
            CourseAssessmentScore scoreRow = scoreMap.get(a.getId());
            BigDecimal score = scoreRow != null ? scoreRow.getValue() : null;
            String scoreStatus = scoreRow != null ? scoreRow.getStatus() : "PENDIENTE";

            // contribution = (score / maxScore) * (weightPercentage / 100) * 20
            BigDecimal contribution = null;
            if (score != null && a.getMaxScore() != null && a.getMaxScore().compareTo(BigDecimal.ZERO) != 0) {
                contribution = score
                        .divide(a.getMaxScore(), 10, RoundingMode.HALF_UP)
                        .multiply(a.getWeightPercentage())
                        .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP)
                        .multiply(BigDecimal.valueOf(20))
                        .setScale(2, RoundingMode.HALF_UP);
                totalContribution = totalContribution.add(contribution);
            }

            items.add(ParentGradesOverviewResponse.GradeItem.builder()
                    .assessmentId(a.getId())
                    .title(a.getTitle())
                    .type(a.getType())
                    .weightPercentage(a.getWeightPercentage())
                    .maxScore(a.getMaxScore())
                    .score(score)
                    .status(scoreStatus)
                    .contributionOver20(contribution)
                    .build());
        }

        return ParentGradesOverviewResponse.builder()
                .studentId(studentId)
                .courseId(courseId)
                .periodId(periodId)
                .periodName(periodName)
                .items(items)
                .currentAverageOver20(totalContribution.setScale(2, RoundingMode.HALF_UP))
                .build();
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    /**
     * Enforce that the calling user IS the parent, unless they are ADMIN or DIRECTOR.
     * ADMIN/DIRECTOR bypass the self-check (they may view any parent's data).
     */
    private void assertSelfOrAdmin(Long parentId) {
        Long callerUserId = TenantContext.getUserId();
        if (callerUserId == null) return;
        if (callerUserId.equals(parentId)) return;

        // Check if caller has ADMIN or DIRECTOR role — if so, allow bypass
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth != null) {
            boolean isPrivileged = auth.getAuthorities().stream()
                    .map(GrantedAuthority::getAuthority)
                    .anyMatch(a -> a.equals("ROLE_ADMIN") || a.equals("ROLE_DIRECTOR"));
            if (isPrivileged) return;
        }

        throw new BusinessException("Access denied: parentId mismatch", HttpStatus.FORBIDDEN, "FORBIDDEN");
    }

    /** Public entry point for controllers that need to validate parent+child without owning the full flow. */
    public void assertParentChildPublic(Long parentId, Long studentId, Long institutionId) {
        assertSelfOrAdmin(parentId);
        assertParentChild(parentId, studentId, institutionId);
    }

    private void assertParentChild(Long parentId, Long studentId, Long institutionId) {
        boolean linked = parentStudentLinkRepo
                .existsByInstitutionIdAndParentIdAndStudentId(institutionId, parentId, studentId);
        if (!linked) {
            throw new BusinessException("Student not linked to parent", HttpStatus.FORBIDDEN, "FORBIDDEN");
        }
    }

    private void assertChildCourse(Long studentId, Long courseId, Long institutionId) {
        List<CourseAssignmentResponse> courses =
                userRelationsService.getStudentCourses(studentId, institutionId);
        boolean found = courses.stream().anyMatch(c -> courseId.equals(c.getId()));
        if (!found) {
            throw new BusinessException("Course not found for student", HttpStatus.FORBIDDEN, "FORBIDDEN");
        }
    }
}
