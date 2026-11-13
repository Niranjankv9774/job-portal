package com.example.jobportal.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableMethodSecurity      // lets us use @PreAuthorize on the REST endpoints later
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // Public pages
                        .requestMatchers("/", "/register", "/login", "/forgot-password", "/reset-password",
                                "/error", "/error/**").permitAll()
                        .requestMatchers("/swagger-ui.html", "/swagger-ui/**", "/v3/api-docs/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/jobs", "/jobs/{id}", "/api/jobs").permitAll()
                        // Only seekers can apply
                        .requestMatchers(HttpMethod.POST, "/jobs/{id}/apply").hasRole("SEEKER")
                        // Role areas
                        .requestMatchers("/recruiter/**").hasRole("RECRUITER")
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers("/seeker/**").hasRole("SEEKER")
                        // Everything else needs a login
                        .anyRequest().authenticated())
                .formLogin(form -> form
                        .loginPage("/login")                       // our own page instead of Spring's
                        .usernameParameter("email")                // the form field is called "email"
                        .defaultSuccessUrl("/dashboard", true)     // where to go after a successful login
                        .permitAll())
                .logout(logout -> logout
                        .logoutSuccessUrl("/login?logout")
                        .permitAll())
                // The REST API (Day 13) skips the CSRF token so tools like Swagger can call it.
                .csrf(csrf -> csrf.ignoringRequestMatchers("/api/**"));
        return http.build();
    }
}