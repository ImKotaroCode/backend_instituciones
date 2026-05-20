package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.AcademicYear;
import backend_instituciones.backend_instituciones.domain.entity.Classroom;
import backend_instituciones.backend_instituciones.domain.entity.ClassroomStudent;
import backend_instituciones.backend_instituciones.domain.entity.GradeLevel;
import backend_instituciones.backend_instituciones.domain.entity.Section;
import backend_instituciones.backend_instituciones.domain.entity.User;
import backend_instituciones.backend_instituciones.dto.request.ClassroomRequest;
import backend_instituciones.backend_instituciones.dto.response.ClassroomResponse;
import backend_instituciones.backend_instituciones.dto.response.PageResponse;
import backend_instituciones.backend_instituciones.dto.response.UserResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.AcademicYearRepository;
import backend_instituciones.backend_instituciones.repository.ClassroomRepository;
import backend_instituciones.backend_instituciones.repository.ClassroomStudentRepository;
import backend_instituciones.backend_instituciones.repository.GradeLevelRepository;
import backend_instituciones.backend_instituciones.repository.SectionRepository;
import backend_instituciones.backend_instituciones.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ClassroomService {

    private final ClassroomRepository classroomRepository;
    private final ClassroomStudentRepository classroomStudentRepository;
    private final GradeLevelRepository gradeLevelRepository;
    private final SectionRepository sectionRepository;
    private final AcademicYearRepository academicYearRepository;
    private final UserRepository userRepository;
    private final UserService userService;

    @Transactional(readOnly = true)
    public PageResponse<ClassroomResponse> list(Long institutionId, String academicYear, int page, int size) {
        var pageable = PageRequest.of(page, size, Sort.by("name").ascending());
        var resultPage = (academicYear != null)
                ? classroomRepository.findByInstitutionIdAndAcademicYear(institutionId, academicYear, pageable)
                : classroomRepository.findByInstitutionId(institutionId, pageable);
        return PageResponse.from(resultPage.map(c -> toResponse(c, institutionId)));
    }

    @Transactional(readOnly = true)
    public ClassroomResponse get(Long id, Long institutionId) {
        Classroom c = findOrThrow(id, institutionId);
        return toResponse(c, institutionId);
    }

    @Transactional
    public ClassroomResponse create(Long institutionId, ClassroomRequest request) {
        Classroom entity = Classroom.builder()
                .institutionId(institutionId)
                .name(request.getName())
                .gradeLevelId(request.getGradeLevelId())
                .sectionId(request.getSectionId())
                .academicYear(request.getAcademicYear())
                .tutorTeacherId(request.getTutorTeacherId())
                .academicYearId(request.getAcademicYearId())
                .capacity(request.getCapacity())
                .build();
        return toResponse(classroomRepository.save(entity), institutionId);
    }

    @Transactional
    public ClassroomResponse update(Long id, Long institutionId, ClassroomRequest request) {
        Classroom entity = findOrThrow(id, institutionId);
        entity.setName(request.getName());
        entity.setGradeLevelId(request.getGradeLevelId());
        entity.setSectionId(request.getSectionId());
        entity.setAcademicYear(request.getAcademicYear());
        entity.setTutorTeacherId(request.getTutorTeacherId());
        entity.setAcademicYearId(request.getAcademicYearId());
        entity.setCapacity(request.getCapacity());
        return toResponse(classroomRepository.save(entity), institutionId);
    }

    @Transactional
    public void delete(Long id, Long institutionId) {
        Classroom entity = findOrThrow(id, institutionId);
        classroomRepository.delete(entity);
    }

    public List<UserResponse> listStudents(Long classroomId, Long institutionId) {
        findOrThrow(classroomId, institutionId);
        return classroomStudentRepository.findByClassroomId(classroomId).stream()
                .map(cs -> userRepository.findById(cs.getStudentId())
                        .map(userService::toResponse).orElse(null))
                .filter(u -> u != null)
                .toList();
    }

    @Transactional
    public void addStudent(Long classroomId, Long studentId, Long institutionId) {
        findOrThrow(classroomId, institutionId);
        userRepository.findById(studentId)
                .filter(u -> u.getInstitutionId().equals(institutionId))
                .orElseThrow(() -> new ResourceNotFoundException("User", studentId));
        if (classroomStudentRepository.existsByClassroomIdAndStudentId(classroomId, studentId)) {
            throw new BusinessException("Student already in classroom", HttpStatus.CONFLICT, "ALREADY_ENROLLED");
        }
        classroomStudentRepository.save(ClassroomStudent.builder()
                .classroomId(classroomId)
                .studentId(studentId)
                .institutionId(institutionId)
                .build());
    }

    @Transactional
    public void removeStudent(Long classroomId, Long studentId, Long institutionId) {
        findOrThrow(classroomId, institutionId);
        classroomStudentRepository.deleteByClassroomIdAndStudentId(classroomId, studentId);
    }

    private Classroom findOrThrow(Long id, Long institutionId) {
        return classroomRepository.findByIdAndInstitutionId(id, institutionId)
                .orElseThrow(() -> new ResourceNotFoundException("Classroom", id));
    }

    private ClassroomResponse toResponse(Classroom c, Long institutionId) {
        String gradeLevelName = null;
        if (c.getGradeLevelId() != null) {
            gradeLevelName = gradeLevelRepository.findById(c.getGradeLevelId())
                    .map(GradeLevel::getName).orElse(null);
        }
        String sectionName = null;
        if (c.getSectionId() != null) {
            sectionName = sectionRepository.findById(c.getSectionId())
                    .map(Section::getName).orElse(null);
        }
        String tutorName = null;
        if (c.getTutorTeacherId() != null) {
            tutorName = userRepository.findById(c.getTutorTeacherId())
                    .map(User::getName).orElse(null);
        }
        String yearName = null;
        if (c.getAcademicYearId() != null) {
            yearName = academicYearRepository.findById(c.getAcademicYearId())
                    .map(AcademicYear::getName).orElse(null);
        }
        long studentCount = classroomStudentRepository.countByClassroomId(c.getId());
        return ClassroomResponse.builder()
                .id(c.getId())
                .institutionId(c.getInstitutionId())
                .name(c.getName())
                .gradeLevelId(c.getGradeLevelId())
                .gradeLevelName(gradeLevelName)
                .sectionId(c.getSectionId())
                .sectionName(sectionName)
                .academicYear(c.getAcademicYear())
                .tutorTeacherId(c.getTutorTeacherId())
                .tutorTeacherName(tutorName)
                .academicYearId(c.getAcademicYearId())
                .academicYearName(yearName)
                .capacity(c.getCapacity())
                .studentCount(studentCount)
                .createdAt(c.getCreatedAt())
                .build();
    }
}
