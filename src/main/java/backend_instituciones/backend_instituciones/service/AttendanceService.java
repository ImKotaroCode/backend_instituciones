package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.Attendance;
import backend_instituciones.backend_instituciones.domain.enums.AttendanceStatus;
import backend_instituciones.backend_instituciones.dto.request.AttendanceBulkRequest;
import backend_instituciones.backend_instituciones.dto.response.AttendanceResponse;
import backend_instituciones.backend_instituciones.exception.ResourceNotFoundException;
import backend_instituciones.backend_instituciones.repository.AttendanceRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@Service
@RequiredArgsConstructor
public class AttendanceService {

    private final AttendanceRepository attendanceRepository;

    public List<AttendanceResponse> getByCourseAndDate(Long courseId, LocalDate date) {
        return attendanceRepository.findByCourseIdAndDate(courseId, date)
                .stream().map(this::toResponse).toList();
    }

    @Transactional
    public List<AttendanceResponse> bulkRegister(Long institutionId, Long registeredBy, AttendanceBulkRequest request) {
        return request.getRecords().stream().map(record -> {
            Attendance attendance = attendanceRepository
                    .findByCourseIdAndStudentIdAndDate(request.getCourseId(), record.getStudentId(), request.getDate())
                    .orElseGet(() -> Attendance.builder()
                            .institutionId(institutionId)
                            .courseId(request.getCourseId())
                            .studentId(record.getStudentId())
                            .date(request.getDate())
                            .build());

            attendance.setStatus(record.getStatus());
            attendance.setRegisteredBy(registeredBy);
            return toResponse(attendanceRepository.save(attendance));
        }).toList();
    }

    @Transactional
    public AttendanceResponse update(Long id, Long institutionId, AttendanceStatus status) {
        Attendance attendance = attendanceRepository.findById(id)
                .filter(a -> a.getInstitutionId().equals(institutionId))
                .orElseThrow(() -> new ResourceNotFoundException("Attendance", id));
        attendance.setStatus(status);
        return toResponse(attendanceRepository.save(attendance));
    }

    @Cacheable(value = "attendance:summary", key = "#studentId + '-' + #institutionId")
    public Map<String, Long> getSummary(Long studentId, Long institutionId) {
        return Map.of(
                "present", attendanceRepository.countByStudentIdAndInstitutionIdAndStatus(studentId, institutionId, AttendanceStatus.PRESENT),
                "absent", attendanceRepository.countByStudentIdAndInstitutionIdAndStatus(studentId, institutionId, AttendanceStatus.ABSENT),
                "late", attendanceRepository.countByStudentIdAndInstitutionIdAndStatus(studentId, institutionId, AttendanceStatus.LATE),
                "justified", attendanceRepository.countByStudentIdAndInstitutionIdAndStatus(studentId, institutionId, AttendanceStatus.JUSTIFIED)
        );
    }

    private AttendanceResponse toResponse(Attendance a) {
        return AttendanceResponse.builder()
                .id(a.getId())
                .courseId(a.getCourseId())
                .studentId(a.getStudentId())
                .date(a.getDate())
                .status(a.getStatus())
                .registeredBy(a.getRegisteredBy())
                .build();
    }
}
