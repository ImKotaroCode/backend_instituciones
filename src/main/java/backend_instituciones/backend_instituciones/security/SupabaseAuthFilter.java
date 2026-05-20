package backend_instituciones.backend_instituciones.security;

import backend_instituciones.backend_instituciones.domain.entity.AdminProfile;
import backend_instituciones.backend_instituciones.domain.entity.User;
import backend_instituciones.backend_instituciones.domain.enums.Role;
import backend_instituciones.backend_instituciones.repository.AdminProfileRepository;
import backend_instituciones.backend_instituciones.repository.UserRepository;
import backend_instituciones.backend_instituciones.service.SaaSCoreService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SupabaseAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;
    private final SaaSCoreService saaSCoreService;
    private final AdminProfileRepository adminProfileRepository;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                String supabaseUid = jwtAuth.getToken().getSubject();

                Optional<User> userOpt = userRepository.findBySupabaseUid(supabaseUid);

                if (userOpt.isPresent()) {
                    User user = userOpt.get();

                    // Block suspended/expired institutions
                    if (!saaSCoreService.isInstitutionActive(user.getInstitutionId())) {
                        response.setStatus(402);
                        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                        response.getWriter().write(
                            "{\"error\":\"SUBSCRIPTION_SUSPENDIDA\"," +
                            "\"message\":\"SUSCRIPCION SUSPENDIDA, CONTACTAR CON EL PROOVEDOR 'KUI' \"," +
                            "\"path\":\"" + request.getRequestURI() + "\"}"
                        );
                        return;
                    }

                    // Populate TenantContext for backward compat (all existing controllers use it)
                    TenantContext.setInstitutionId(user.getInstitutionId());
                    TenantContext.setUserId(user.getId());

                    // Populate AuthUserContext for new role checks
                    AuthUserContext.set(new AuthUser(supabaseUid, user.getInstitutionId(), user.getId(), user.getRole()));

                    // Replace authentication with one carrying DB role authorities
                    var newAuth = new UsernamePasswordAuthenticationToken(
                            supabaseUid,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );
                    newAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(newAuth);

                    // Component-level permission check for ADMINISTRACION role
                    if (user.getRole() == Role.ADMINISTRACION) {
                        String module = resolveModule(request.getRequestURI());
                        if (module != null && !hasComponentAccess(user.getId(), module)) {
                            response.setStatus(403);
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.getWriter().write("{\"message\":\"No tienes acceso a este componente\"}");
                            return;
                        }
                    }
                }
            }

            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            AuthUserContext.clear();
        }
    }

    private String resolveModule(String uri) {
        if (uri.startsWith("/api/v1/users")) return "USUARIOS";
        if (uri.startsWith("/api/v1/payments")) return "PAGOS";
        if (uri.startsWith("/api/v1/announcements")) return "ANUNCIOS";
        if (uri.startsWith("/api/v1/attendance") || uri.startsWith("/api/v1/teacher-attendance")) return "ASISTENCIA";
        if (uri.startsWith("/api/v1/warehouse")) return "ALMACEN";
        if (uri.startsWith("/api/v1/grade-levels")
                || uri.startsWith("/api/v1/sections")
                || uri.startsWith("/api/v1/classrooms")
                || uri.startsWith("/api/v1/course-assignments")
                || uri.startsWith("/api/v1/course-catalog")
                || uri.startsWith("/api/v1/academic-structure")
                || uri.startsWith("/api/v1/academic-years")
                || uri.startsWith("/api/v1/enrollments")
                || uri.startsWith("/api/v1/academic-periods")) return "ESTRUCTURA";
        return null; // no module restriction (dashboard, profile, auth/me, etc.)
    }

    private boolean hasComponentAccess(Long userId, String module) {
        return adminProfileRepository.findByUserId(userId)
                .map(AdminProfile::getAdminPermissions)
                .map(perms -> {
                    Map<String, Boolean> modulePerms = perms.get(module);
                    return modulePerms != null && Boolean.TRUE.equals(modulePerms.get("access"));
                })
                .orElse(false);
    }
}
