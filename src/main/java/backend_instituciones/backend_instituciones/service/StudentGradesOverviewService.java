package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.AcademicPeriod;
import backend_instituciones.backend_instituciones.domain.entity.CourseAssessment;
import backend_instituciones.backend_instituciones.domain.entity.CourseAssessmentScore;
import backend_instituciones.backend_instituciones.dto.response.CourseAssignmentResponse;
import backend_instituciones.backend_instituciones.dto.response.StudentGradesOverviewResponse;
import backend_instituciones.backend_instituciones.repository.AcademicPeriodRepository;
import backend_instituciones.backend_instituciones.repository.CourseAssessmentRepository;
import backend_instituciones.backend_instituciones.repository.CourseAssessmentScoreRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class StudentGradesOverviewService {

    private final UserRelationsService userRelationsService;
    private final CourseAssessmentRepository assessmentRepo;
    private final CourseAssessmentScoreRepository scoreRepo;
    private final AcademicPeriodRepository periodRepo;

    @Transactional(readOnly = true)
    public StudentGradesOverviewResponse getOverview(Long institutionId, Long studentId,
                                                      Long periodId, Long academicYearId) {
        List<CourseAssignmentResponse> courses =
                userRelationsService.getStudentCourses(studentId, institutionId);

        if (courses.isEmpty()) {
            return StudentGradesOverviewResponse.builder()
                    .studentId(studentId)
                    .academicYearId(academicYearId)
                    .periods(List.of())
                    .build();
        }

        Map<Long, CourseAssignmentResponse> courseMap = courses.stream()
                .collect(Collectors.toMap(CourseAssignmentResponse::getId, Function.identity()));

        List<Long> courseIds = new ArrayList<>(courseMap.keySet());

        // Batch load all assessments for all student courses
        List<CourseAssessment> allAssessments =
                assessmentRepo.findByCourseIdInAndInstitutionId(courseIds, institutionId);

        // Filter by periodId if given
        if (periodId != null) {
            allAssessments = allAssessments.stream()
                    .filter(a -> periodId.equals(a.getPeriodId()))
                    .toList();
        }

        // Only assessments with a known period
        allAssessments = allAssessments.stream()
                .filter(a -> a.getPeriodId() != null)
                .toList();

        if (allAssessments.isEmpty()) {
            return StudentGradesOverviewResponse.builder()
                    .studentId(studentId)
                    .academicYearId(academicYearId)
                    .periods(List.of())
                    .build();
        }

        // Batch load student scores for all assessments
        List<Long> assessmentIds = allAssessments.stream().map(CourseAssessment::getId).toList();
        Map<Long, CourseAssessmentScore> scoreMap =
                scoreRepo.findByAssessmentIdInAndStudentId(assessmentIds, studentId).stream()
                        .collect(Collectors.toMap(CourseAssessmentScore::getAssessmentId, Function.identity()));

        // Load period entities for the periodIds found in assessments
        Set<Long> foundPeriodIds = allAssessments.stream()
                .map(CourseAssessment::getPeriodId)
                .collect(Collectors.toSet());
        Map<Long, AcademicPeriod> periodEntityMap = periodRepo.findAllById(foundPeriodIds).stream()
                .filter(p -> p.getInstitutionId().equals(institutionId))
                .collect(Collectors.toMap(AcademicPeriod::getId, Function.identity()));

        // Group assessments by periodId → then by courseId
        Map<Long, List<CourseAssessment>> byPeriod = allAssessments.stream()
                .collect(Collectors.groupingBy(CourseAssessment::getPeriodId));

        List<StudentGradesOverviewResponse.PeriodSummary> periodSummaries = new ArrayList<>();

        for (Map.Entry<Long, List<CourseAssessment>> periodEntry : byPeriod.entrySet()) {
            Long pid = periodEntry.getKey();
            AcademicPeriod period = periodEntityMap.get(pid);
            if (period == null) continue;

            Map<Long, List<CourseAssessment>> byCourse = periodEntry.getValue().stream()
                    .collect(Collectors.groupingBy(CourseAssessment::getCourseId));

            List<StudentGradesOverviewResponse.CourseSummary> courseSummaries = new ArrayList<>();
            BigDecimal averageSum = BigDecimal.ZERO;

            for (Map.Entry<Long, List<CourseAssessment>> courseEntry : byCourse.entrySet()) {
                Long cid = courseEntry.getKey();
                CourseAssignmentResponse course = courseMap.get(cid);
                if (course == null) continue;

                List<CourseAssessment> assessments = courseEntry.getValue();

                BigDecimal configuredWeight = BigDecimal.ZERO;
                BigDecimal scoredWeight = BigDecimal.ZERO;
                BigDecimal currentAverage = BigDecimal.ZERO;
                int scoredCount = 0;

                for (CourseAssessment a : assessments) {
                    BigDecimal w = a.getWeightPercentage() != null
                            ? a.getWeightPercentage() : BigDecimal.ZERO;
                    configuredWeight = configuredWeight.add(w);

                    CourseAssessmentScore sc = scoreMap.get(a.getId());
                    if (sc != null && sc.getValue() != null) {
                        scoredWeight = scoredWeight.add(w);
                        scoredCount++;
                        // contribution = score * weightPercentage / 100
                        BigDecimal contribution = sc.getValue()
                                .multiply(w)
                                .divide(BigDecimal.valueOf(100), 10, RoundingMode.HALF_UP);
                        currentAverage = currentAverage.add(contribution);
                    }
                }

                currentAverage = currentAverage.setScale(2, RoundingMode.HALF_UP);
                averageSum = averageSum.add(currentAverage);

                courseSummaries.add(StudentGradesOverviewResponse.CourseSummary.builder()
                        .courseId(cid)
                        .courseName(course.getCourseName())
                        .teacherId(course.getTeacherUserId())
                        .teacherName(course.getTeacherName())
                        .sectionId(course.getSectionId())
                        .sectionName(course.getSection())
                        .levelName(course.getEducationLevel())
                        .gradeName(course.getGrade())
                        .currentAverage(currentAverage)
                        .configuredWeight(configuredWeight.setScale(2, RoundingMode.HALF_UP))
                        .scoredWeight(scoredWeight.setScale(2, RoundingMode.HALF_UP))
                        .scoredCount(scoredCount)
                        .totalCount(assessments.size())
                        .build());
            }

            if (courseSummaries.isEmpty()) continue;

            courseSummaries.sort(Comparator.comparing(
                    StudentGradesOverviewResponse.CourseSummary::getCourseName,
                    Comparator.nullsLast(Comparator.naturalOrder())));

            BigDecimal generalAverage = averageSum
                    .divide(BigDecimal.valueOf(courseSummaries.size()), 2, RoundingMode.HALF_UP);

            periodSummaries.add(StudentGradesOverviewResponse.PeriodSummary.builder()
                    .periodId(pid)
                    .periodName(period.getName())
                    .startDate(period.getStartDate())
                    .endDate(period.getEndDate())
                    .generalAverage(generalAverage)
                    .courseCount(courseSummaries.size())
                    .courses(courseSummaries)
                    .build());
        }

        periodSummaries.sort(Comparator.comparing(
                StudentGradesOverviewResponse.PeriodSummary::getStartDate,
                Comparator.nullsLast(Comparator.naturalOrder())));

        return StudentGradesOverviewResponse.builder()
                .studentId(studentId)
                .academicYearId(academicYearId)
                .periods(periodSummaries)
                .build();
    }
}
