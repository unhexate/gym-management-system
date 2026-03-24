package com.gym.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.password.NoOpPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration.
 *
 * Auth mechanism : HTTP Basic (stateless – no server-side sessions).
 * Password storage: plain-text via NoOpPasswordEncoder (passwords are stored
 *                   as-is in the DB; acceptable for this demo project).
 * CORS            : wide-open so the vanilla-JS frontend can reach the API.
 *
 * Role-to-endpoint mapping
 * ────────────────────────
 *  ADMIN        – all endpoints
 *  RECEPTIONIST – POST /api/users, POST /api/memberships,
 *                 POST /api/payments, POST /api/attendance,
 *                 all GET endpoints
 *  TRAINER      – POST /api/workouts, all GET endpoints
 *  MEMBER       – all GET endpoints, PUT /api/users/{id}/profile
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .httpBasic(httpBasic -> {})
            .authorizeHttpRequests(auth -> auth

                // ── /api/users/me – any authenticated user can fetch their own info
                .requestMatchers(HttpMethod.GET, "/api/users/me").authenticated()

                // ── User registration – public (anyone can self-register)
                .requestMatchers(HttpMethod.POST, "/api/users").permitAll()

                // ── Profile update – any authenticated user (service layer enforces ownership)
                .requestMatchers(HttpMethod.PUT, "/api/users/**").authenticated()

                // ── Memberships
                .requestMatchers(HttpMethod.POST, "/api/memberships").hasAnyRole("ADMIN", "RECEPTIONIST")
                .requestMatchers(HttpMethod.POST, "/api/memberships/me").hasRole("MEMBER")
                .requestMatchers(HttpMethod.POST, "/api/memberships/me/purchase").hasRole("MEMBER")
                .requestMatchers(HttpMethod.GET,  "/api/memberships/me").hasRole("MEMBER")
                .requestMatchers(HttpMethod.GET,  "/api/memberships/member/**").hasAnyRole("ADMIN", "RECEPTIONIST", "TRAINER")

                // ── Payments
                .requestMatchers(HttpMethod.POST, "/api/payments").hasAnyRole("ADMIN", "RECEPTIONIST")
                .requestMatchers(HttpMethod.POST, "/api/payments/request").hasRole("MEMBER")
                .requestMatchers(HttpMethod.PUT,  "/api/payments/*/status").hasAnyRole("ADMIN", "RECEPTIONIST")
                .requestMatchers(HttpMethod.GET,  "/api/payments/pending").hasAnyRole("ADMIN", "RECEPTIONIST")
                .requestMatchers(HttpMethod.GET,  "/api/payments/me").hasRole("MEMBER")
                .requestMatchers(HttpMethod.GET,  "/api/payments/member/**").hasAnyRole("ADMIN", "RECEPTIONIST")

                // ── Workouts
                .requestMatchers(HttpMethod.POST, "/api/workouts").hasAnyRole("ADMIN", "TRAINER")
                .requestMatchers(HttpMethod.GET,  "/api/workouts/me").hasRole("MEMBER")
                .requestMatchers(HttpMethod.GET,  "/api/workouts/member/**").hasAnyRole("ADMIN", "TRAINER")

                // ── Attendance
                .requestMatchers(HttpMethod.POST, "/api/attendance").hasAnyRole("ADMIN", "RECEPTIONIST")
                .requestMatchers(HttpMethod.GET,  "/api/attendance/me").hasRole("MEMBER")
                .requestMatchers(HttpMethod.GET,  "/api/attendance/member/**").hasAnyRole("ADMIN", "RECEPTIONIST", "TRAINER")

                // ── Reports – Admin only
                .requestMatchers(HttpMethod.GET, "/api/reports").hasRole("ADMIN")

                // ── Deny anything else
                .anyRequest().authenticated()
            );

        return http.build();
    }

    /**
     * Plain-text password encoder.
     * Passwords are stored as-is in the database; new users register with
     * whatever password they provide. Suitable for a demo/course project.
     */
    @Bean
    @SuppressWarnings("deprecation")
    public PasswordEncoder passwordEncoder() {
        return NoOpPasswordEncoder.getInstance();
    }

    /**
     * Permissive CORS so the file:// or localhost frontend can call the API
     * with credentials (required for HTTP Basic auth in browsers).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration cfg = new CorsConfiguration();
        cfg.setAllowedOriginPatterns(List.of("*"));
        cfg.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        cfg.setAllowedHeaders(List.of("*"));
        cfg.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/api/**", cfg);
        return source;
    }
}
