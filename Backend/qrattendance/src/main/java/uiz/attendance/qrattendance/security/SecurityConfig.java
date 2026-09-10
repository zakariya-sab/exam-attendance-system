package uiz.attendance.qrattendance.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Requires a valid JWT for writes to /api/exam-blocks (POST/PUT/DELETE), for
 * the attendance CSV export (GET /api/exam-blocks/{id}/attendance/export),
 * for the catalog reads (GET /api/majors, GET /api/modules), and for
 * generating a student's encrypted attendance QR code
 * (GET /api/EncrypteQrcode/{codeApogee}). Everything else — including the
 * plain GET list of exam blocks, login, and attendance scanning — stays
 * exactly as open as it was before this filter chain existed.
 */
@Configuration
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                           JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.jwtAuthenticationEntryPoint = jwtAuthenticationEntryPoint;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(AbstractHttpConfigurer::disable)
                .cors(cors -> {})
                // .headers(headers -> headers.frameOptions(frameOptions -> frameOptions.sameOrigin()))
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .exceptionHandling(exceptionHandling -> exceptionHandling.authenticationEntryPoint(jwtAuthenticationEntryPoint))
                .authorizeHttpRequests(authorize -> authorize
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/exam-blocks/*/attendance/export").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/majors/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/modules/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/api/students/*/qr-code").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/exam-blocks/**").authenticated()
                        .requestMatchers(HttpMethod.PUT, "/api/exam-blocks/**").authenticated()
                        .requestMatchers(HttpMethod.DELETE, "/api/exam-blocks/**").authenticated()
                        .anyRequest().permitAll())
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
