package backend_instituciones.backend_instituciones.service;

import backend_instituciones.backend_instituciones.domain.entity.AdminActionLog;
import backend_instituciones.backend_instituciones.repository.AdminActionLogRepository;
import backend_instituciones.backend_instituciones.security.TenantContext;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AdminActionLogService {

    private final AdminActionLogRepository repository;

    public void log(Long userId, String userRole, String module, String action,
                    String entityType, Long entityId, String summary) {
        String ip = null;
        String userAgent = null;
        try {
            ServletRequestAttributes attrs = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
            if (attrs != null) {
                HttpServletRequest req = attrs.getRequest();
                ip = req.getRemoteAddr();
                userAgent = truncate(req.getHeader("User-Agent"), 300);
            }
        } catch (Exception ignored) {}

        repository.save(AdminActionLog.builder()
                .institutionId(TenantContext.getInstitutionId())
                .userId(userId)
                .userRole(userRole)
                .module(module)
                .action(action)
                .entityType(entityType)
                .entityId(entityId)
                .summary(truncate(summary, 180))
                .ipAddress(ip)
                .userAgent(userAgent)
                .build());
    }

    public Page<AdminActionLog> list(Long institutionId, Long userId, String module,
                                     LocalDate dateFrom, LocalDate dateTo, int page, int size) {
        LocalDateTime from = dateFrom != null ? dateFrom.atStartOfDay() : null;
        LocalDateTime to = dateTo != null ? dateTo.atTime(23, 59, 59) : null;
        return repository.search(institutionId, userId, module, from, to, PageRequest.of(page, size));
    }

    private String truncate(String s, int max) {
        if (s == null) return null;
        return s.length() > max ? s.substring(0, max) : s;
    }
}
