package backend_instituciones.backend_instituciones.config;

import backend_instituciones.backend_instituciones.security.SupabaseAuthFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.AuthorizationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.time.Instant;
import java.util.Arrays;
import java.util.List;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final SupabaseAuthFilter supabaseAuthFilter;

    @Value("${app.frontend-url:http://localhost:5173}")
    private String frontendUrl;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(AbstractHttpConfigurer::disable)
            .cors(c -> c.configurationSource(corsConfigurationSource()))
            .sessionManagement(s -> s.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                    .requestMatchers(
                            "/api/v1/public/**",
                            "/api/v1/sse/**",
                            "/api/ws/**",
                            "/api/internal/**",
                            "/actuator/health",
                            "/actuator/info",
                            "/swagger-ui/**",
                            "/v3/api-docs/**"
                    ).permitAll()
                    .requestMatchers("/api/v1/provider/**").hasRole("PROVEEDOR")
                    .requestMatchers("/api/v1/users/**").hasRole("ADMIN")
                    .requestMatchers(HttpMethod.GET, "/api/v1/reports/**").hasAnyRole("ADMIN", "DIRECTOR")
                    .requestMatchers("/api/v1/announcements/**").hasAnyRole("ADMIN", "DIRECTOR")
                    .requestMatchers("/api/v1/grade-levels/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE")
                    .requestMatchers("/api/v1/sections/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE")
                    .requestMatchers("/api/v1/classrooms/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE")
                    .requestMatchers("/api/v1/course-assignments/**").hasAnyRole("ADMIN", "DIRECTOR")
                    .requestMatchers("/api/v1/course-catalog/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE")
                    .requestMatchers("/api/v1/academic-structure/**").authenticated()
                    .requestMatchers("/api/v1/imports/**").hasRole("ADMIN")
                    .requestMatchers("/api/v1/students/**").hasAnyRole("ADMIN", "DIRECTOR")
                    .requestMatchers("/api/v1/guardians/**").hasAnyRole("ADMIN", "DIRECTOR", "PADRE")
                    .requestMatchers("/api/v1/enrollments/**").hasAnyRole("ADMIN", "DIRECTOR")
                    .requestMatchers("/api/v1/academic-years/**").hasAnyRole("ADMIN", "DIRECTOR")
                    .requestMatchers("/api/v1/profile/**").authenticated()
                    .requestMatchers("/api/v1/auth/me").authenticated()
                    .anyRequest().authenticated()
            )
            .oauth2ResourceServer(oauth -> oauth.jwt(Customizer.withDefaults()))
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((req, res, e) -> {
                    res.setStatus(401);
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.getWriter().write(
                        "{\"error\":\"UNAUTHORIZED\",\"message\":\"" + e.getMessage() +
                        "\",\"path\":\"" + req.getRequestURI() +
                        "\",\"timestamp\":\"" + Instant.now() + "\"}");
                })
                .accessDeniedHandler((req, res, e) -> {
                    res.setStatus(403);
                    res.setContentType(MediaType.APPLICATION_JSON_VALUE);
                    res.getWriter().write(
                        "{\"error\":\"FORBIDDEN\",\"message\":\"Access denied\",\"path\":\"" +
                        req.getRequestURI() + "\",\"timestamp\":\"" + Instant.now() + "\"}");
                })
            )
            // SupabaseAuthFilter runs before AuthorizationFilter but after JWT validation:
            // BearerTokenAuthenticationFilter already set JwtAuthenticationToken in SecurityContext
            // → load user from DB → set TenantContext + DB roles
            .addFilterBefore(supabaseAuthFilter, AuthorizationFilter.class);

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOrigins(frontendOrigins());
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"));
        config.setAllowedHeaders(List.of("*"));
        config.setAllowCredentials(false);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    private List<String> frontendOrigins() {
        return Arrays.stream(frontendUrl.split(","))
                .map(String::trim)
                .filter(s -> !s.isBlank())
                .toList();
    }
}
