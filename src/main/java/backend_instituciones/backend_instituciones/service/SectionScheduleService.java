package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.SectionScheduleSlot;
import backend_instituciones.backend_instituciones.dto.request.SectionScheduleRequest;
import backend_instituciones.backend_instituciones.dto.request.SectionScheduleSlotRequest;
import backend_instituciones.backend_instituciones.dto.response.SectionScheduleResponse;
import backend_instituciones.backend_instituciones.dto.response.SectionScheduleSlotResponse;
import backend_instituciones.backend_instituciones.exception.BusinessException;
import backend_instituciones.backend_instituciones.repository.SectionScheduleSlotRepository;
import backend_instituciones.backend_instituciones.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Slf4j
@Transactional
public class SectionScheduleService {

    private final SectionScheduleSlotRepository sectionScheduleSlotRepository;
    private final UserRepository userRepository;

    @Transactional(readOnly = true)
    public SectionScheduleResponse getSchedule(Long institutionId, Long sectionId) {
        List<SectionScheduleSlot> slots =
                sectionScheduleSlotRepository.findByInstitutionIdAndSectionId(institutionId, sectionId);

        Map<Long, String> userNames = buildUserNameMap(slots.stream()
                .map(SectionScheduleSlot::getTeacherId).collect(Collectors.toSet()));

        List<SectionScheduleSlotResponse> slotResponses = slots.stream()
                .map(s -> toSlotResponse(s, userNames))
                .toList();

        return SectionScheduleResponse.builder()
                .sectionId(sectionId)
                .slots(slotResponses)
                .build();
    }

    @Transactional(readOnly = true)
    public Map<Long, List<SectionScheduleSlotResponse>> getBatch(Long institutionId,
                                                                   List<Long> sectionIds) {
        if (sectionIds == null || sectionIds.isEmpty()) return Map.of();
        List<SectionScheduleSlot> slots =
                sectionScheduleSlotRepository.findByInstitutionIdAndSectionIdIn(institutionId, sectionIds);

        Map<Long, String> userNames = buildUserNameMap(slots.stream()
                .map(SectionScheduleSlot::getTeacherId).collect(Collectors.toSet()));

        return slots.stream()
                .collect(Collectors.groupingBy(
                        SectionScheduleSlot::getSectionId,
                        Collectors.mapping(s -> toSlotResponse(s, userNames), Collectors.toList())));
    }

    public SectionScheduleResponse updateSchedule(Long institutionId, Long sectionId,
                                                   SectionScheduleRequest request) {
        validateNoOverlap(request.getSlots());

        sectionScheduleSlotRepository.deleteByInstitutionIdAndSectionId(institutionId, sectionId);

        List<SectionScheduleSlot> saved = request.getSlots().stream()
                .map(req -> {
                    SectionScheduleSlot slot = SectionScheduleSlot.builder()
                            .institutionId(institutionId)
                            .sectionId(sectionId)
                            .gradeId(req.getGradeId())
                            .levelId(req.getLevelId())
                            .courseId(req.getCourseId())
                            .courseName(req.getCourseName())
                            .teacherId(req.getTeacherId())
                            .weekday(req.getWeekday().toUpperCase())
                            .startTime(LocalTime.parse(req.getStartTime()))
                            .endTime(LocalTime.parse(req.getEndTime()))
                            .classroomName(req.getClassroomName())
                            .build();
                    return sectionScheduleSlotRepository.save(slot);
                })
                .toList();

        Map<Long, String> userNames = buildUserNameMap(saved.stream()
                .map(SectionScheduleSlot::getTeacherId).collect(Collectors.toSet()));

        List<SectionScheduleSlotResponse> slotResponses = saved.stream()
                .map(s -> toSlotResponse(s, userNames))
                .toList();

        return SectionScheduleResponse.builder()
                .sectionId(sectionId)
                .slots(slotResponses)
                .build();
    }

    public SectionScheduleSlotResponse toSlotResponse(SectionScheduleSlot slot,
                                                       Map<Long, String> userNames) {
        return SectionScheduleSlotResponse.builder()
                .id(slot.getId())
                .sectionId(slot.getSectionId())
                .gradeId(slot.getGradeId())
                .levelId(slot.getLevelId())
                .courseId(slot.getCourseId())
                .courseName(slot.getCourseName() != null ? slot.getCourseName() : "")
                .teacherId(slot.getTeacherId())
                .teacherName(userNames.getOrDefault(slot.getTeacherId(), ""))
                .weekday(slot.getWeekday())
                .startTime(slot.getStartTime() != null ? slot.getStartTime().toString() : null)
                .endTime(slot.getEndTime() != null ? slot.getEndTime().toString() : null)
                .classroomName(slot.getClassroomName())
                .build();
    }

    private void validateNoOverlap(List<SectionScheduleSlotRequest> slots) {
        for (int i = 0; i < slots.size(); i++) {
            SectionScheduleSlotRequest a = slots.get(i);
            LocalTime aStart = LocalTime.parse(a.getStartTime());
            LocalTime aEnd = LocalTime.parse(a.getEndTime());
            for (int j = i + 1; j < slots.size(); j++) {
                SectionScheduleSlotRequest b = slots.get(j);
                if (!a.getWeekday().equalsIgnoreCase(b.getWeekday())) continue;
                LocalTime bStart = LocalTime.parse(b.getStartTime());
                LocalTime bEnd = LocalTime.parse(b.getEndTime());
                if (aStart.isBefore(bEnd) && bStart.isBefore(aEnd)) {
                    throw new BusinessException(
                            "Schedule overlap detected for weekday " + a.getWeekday() +
                            " between " + a.getStartTime() + "-" + a.getEndTime() +
                            " and " + b.getStartTime() + "-" + b.getEndTime(),
                            HttpStatus.BAD_REQUEST, "SCHEDULE_OVERLAP");
                }
            }
        }
    }

    private Map<Long, String> buildUserNameMap(Set<Long> userIds) {
        return userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(u -> u.getId(), u -> u.getName()));
    }
}
