package com.example.onlinecourse.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.HashMap;
import java.util.Map;

/**
 * Spring Security configuration implementing JWT stateless authentication,
 * BCrypt password hashing, and role-based endpoint authorization.
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
    }

    @Bean
    public BCryptPasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(Customizer.withDefaults())
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public Static Resources & HTML Pages
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/*.html",
                                "/css/**",
                                "/js/**",
                                "/favicon.ico",
                                "/images/**"
                        ).permitAll()

                        // Public Login & Registration Endpoints
                        .requestMatchers(
                                "/api/admin/login",
                                "/api/students/login",
                                "/api/admin/faculty/login",
                                "/api/students/register",
                                "/api/auth/**"
                        ).permitAll()

                        // Admin Only Management APIs
                        .requestMatchers("/api/admin/**").hasRole("ADMIN")

                        // Branch, Minor Stream, Section, Semester, Registration Window modifications restricted to ADMIN
                        .requestMatchers(HttpMethod.POST, "/api/branches/**", "/api/minor-streams/**", "/api/sections/**", "/api/semesters/**", "/api/registration-windows/**", "/api/courses/**", "/api/faculty/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/branches/**", "/api/minor-streams/**", "/api/sections/**", "/api/semesters/**", "/api/registration-windows/**", "/api/courses/**", "/api/faculty/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/api/branches/**", "/api/minor-streams/**", "/api/sections/**", "/api/semesters/**", "/api/registration-windows/**", "/api/courses/**", "/api/faculty/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/branches/**", "/api/minor-streams/**", "/api/sections/**", "/api/semesters/**", "/api/registration-windows/**", "/api/courses/**", "/api/faculty/**").hasRole("ADMIN")

                        // Read permissions for authenticated academic actors
                        .requestMatchers(HttpMethod.GET, "/api/branches/**", "/api/minor-streams/**", "/api/semesters/**", "/api/courses/**", "/api/registration-windows/**", "/api/sections/**").authenticated()

                        // Faculty APIs
                        .requestMatchers("/api/faculty/**").hasAnyRole("FACULTY", "ADMIN")

                        // Student APIs
                        .requestMatchers("/api/registrations/**").hasAnyRole("STUDENT", "ADMIN")
                        .requestMatchers("/api/students/**").hasAnyRole("STUDENT", "ADMIN")

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            Map<String, Object> body = new HashMap<>();
                            body.put("status", 401);
                            body.put("error", "Unauthorized");
                            body.put("message", "Full authentication is required to access this resource: " + authException.getMessage());
                            body.put("path", request.getRequestURI());
                            new ObjectMapper().writeValue(response.getOutputStream(), body);
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            response.setContentType(MediaType.APPLICATION_JSON_VALUE);
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            Map<String, Object> body = new HashMap<>();
                            body.put("status", 403);
                            body.put("error", "Forbidden");
                            body.put("message", "Access denied: You do not possess the required role for this resource.");
                            body.put("path", request.getRequestURI());
                            new ObjectMapper().writeValue(response.getOutputStream(), body);
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
