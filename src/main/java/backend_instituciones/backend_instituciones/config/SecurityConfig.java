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
import com.nimbusds.jose.JWSAlgorithm;
import com.nimbusds.jose.jwk.JWKSet;
import com.nimbusds.jose.jwk.source.ImmutableJWKSet;
import com.nimbusds.jose.proc.JWSVerificationKeySelector;
import com.nimbusds.jwt.proc.DefaultJWTProcessor;
import org.springframework.core.io.ClassPathResource;
import org.springframework.security.oauth2.jose.jws.SignatureAlgorithm;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.util.StreamUtils;
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

    @Value("${spring.security.oauth2.resourceserver.jwt.jwk-set-uri}")
    private String jwkSetUri;

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
                            "/v3/api-docs/**",
                            "/error"
                    ).permitAll()
                    .requestMatchers("/api/v1/provider/**").hasRole("PROVEEDOR")
                    .requestMatchers("/api/v1/users/**").hasAnyRole("ADMIN", "DIRECTOR", "ALMACEN", "ADMINISTRACION")
                    .requestMatchers(HttpMethod.GET, "/api/v1/reports/**").hasAnyRole("ADMIN", "DIRECTOR", "ADMINISTRACION")
                    .requestMatchers("/api/v1/announcements/**").hasAnyRole("ADMIN", "DIRECTOR", "PADRE", "DOCENTE", "ESTUDIANTE", "ALMACEN", "ADMINISTRACION")
                    .requestMatchers("/api/v1/grade-levels/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/sections/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/classrooms/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/course-assignments/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE", "ESTUDIANTE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/course-catalog/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/academic-structure/**").authenticated()
                    .requestMatchers("/api/v1/imports/**").hasAnyRole("ADMIN", "ADMINISTRACION")
                    .requestMatchers("/api/v1/students/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE", "ESTUDIANTE", "PADRE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/teachers/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/guardians/**").hasAnyRole("ADMIN", "DIRECTOR", "PADRE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/enrollments/**").hasAnyRole("ADMIN", "DIRECTOR", "ADMINISTRACION")
                    .requestMatchers("/api/v1/academic-years/**").hasAnyRole("ADMIN", "DIRECTOR", "PROVEEDOR", "ADMINISTRACION")
                    .requestMatchers("/api/v1/profile/**").authenticated()
                    .requestMatchers("/api/v1/auth/me").authenticated()
                    .requestMatchers("/api/v1/courses/*/content").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE", "ESTUDIANTE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/courses/*/content/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE", "ADMINISTRACION")
                    .requestMatchers(HttpMethod.GET, "/api/v1/section-schedules/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE", "PROVEEDOR", "ESTUDIANTE", "PADRE", "ADMINISTRACION")
                    .requestMatchers(HttpMethod.PUT, "/api/v1/section-schedules/**").hasAnyRole("ADMIN", "DIRECTOR", "PROVEEDOR", "PADRE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/dashboard/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE", "ESTUDIANTE", "PADRE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/courses/*/tasks/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE", "ESTUDIANTE", "PADRE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/courses/*/assessments/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE", "ESTUDIANTE", "PADRE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/tasks/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE", "ESTUDIANTE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/assessments/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE", "ESTUDIANTE", "PADRE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/teacher-attendance/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/attendance/**").hasAnyRole("ADMIN", "DIRECTOR", "DOCENTE", "ESTUDIANTE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/attendance-center/**").hasAnyRole("ADMIN", "DIRECTOR", "ADMINISTRACION")
                    .requestMatchers("/api/v1/payments/**").hasAnyRole("ADMIN", "DIRECTOR", "PADRE", "ADMINISTRACION")
                    .requestMatchers("/api/v1/parents/**").hasAnyRole("PADRE", "ADMIN", "DIRECTOR", "ADMINISTRACION")
                    .requestMatchers("/api/v1/warehouse/**").hasAnyRole("ADMIN", "DIRECTOR", "ALMACEN", "ADMINISTRACION")
                    .requestMatchers(HttpMethod.GET, "/api/v1/academic-periods/**").authenticated()
                    .requestMatchers(HttpMethod.PUT, "/api/v1/academic-periods/**").hasAnyRole("ADMIN", "DIRECTOR", "ADMINISTRACION")
                    .requestMatchers("/api/v1/admin-action-logs/**").hasAnyRole("ADMIN", "ADMINISTRACION")
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
    public JwtDecoder jwtDecoder() throws Exception {
        ClassPathResource jwksResource = new ClassPathResource("supabase-jwks.json");
        String jwksJson = StreamUtils.copyToString(jwksResource.getInputStream(), java.nio.charset.StandardCharsets.UTF_8);
        JWKSet jwkSet = JWKSet.parse(jwksJson);
        var jwkSource = new ImmutableJWKSet<com.nimbusds.jose.proc.SecurityContext>(jwkSet);
        var keySelector = new JWSVerificationKeySelector<com.nimbusds.jose.proc.SecurityContext>(JWSAlgorithm.ES256, jwkSource);
        var processor = new DefaultJWTProcessor<com.nimbusds.jose.proc.SecurityContext>();
        processor.setJWSKeySelector(keySelector);
        // Disable audience/claims verification — Supabase tokens have aud:"authenticated"
        // which Nimbus DefaultJWTClaimsVerifier rejects without explicit audience config
        processor.setJWTClaimsSetVerifier(null);
        return new NimbusJwtDecoder(processor);
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
