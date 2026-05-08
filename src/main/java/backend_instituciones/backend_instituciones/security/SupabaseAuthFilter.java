package backend_instituciones.backend_instituciones.security;

import backend_instituciones.backend_instituciones.domain.entity.User;
import backend_instituciones.backend_instituciones.repository.UserRepository;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
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
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class SupabaseAuthFilter extends OncePerRequestFilter {

    private final UserRepository userRepository;

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

                    // Populate TenantContext for backward compat (all existing controllers use it)
                    TenantContext.setInstitutionId(user.getInstitutionId());
                    TenantContext.setUserId(user.getId());

                    // Populate AuthUserContext for new role checks
                    AuthUserContext.set(new AuthUser(supabaseUid, user.getInstitutionId(), user.getId(), user.getRole()));

                    // Replace authentication with one carrying DB role authorities
                    // (enables existing hasRole() rules in SecurityConfig)
                    var newAuth = new UsernamePasswordAuthenticationToken(
                            supabaseUid,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                    );
                    newAuth.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                    SecurityContextHolder.getContext().setAuthentication(newAuth);
                }
            }

            chain.doFilter(request, response);
        } finally {
            TenantContext.clear();
            AuthUserContext.clear();
        }
    }
}
