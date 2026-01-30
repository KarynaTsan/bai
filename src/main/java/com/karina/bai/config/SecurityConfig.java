package com.karina.bai.config;

import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;
import org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter;

@Configuration
public class SecurityConfig {
    private static final Logger log = LoggerFactory.getLogger(SecurityConfig.class);


    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http

                .addFilterBefore(rateLimitFilter(), UsernamePasswordAuthenticationFilter.class)

                .csrf(csrf -> {
                }) //ochrona CSRF
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers("/login", "/register").permitAll()
                        .requestMatchers("/css/**", "/js/**", "/images/**").permitAll() //на всякий пожарный
                        .requestMatchers("/admin/**").hasRole("ADMIN") //reguły autoryzacji
                        .requestMatchers("/user/**").hasRole("USER")
                        .anyRequest().authenticated()

                )
                .formLogin(form -> form
                        .loginPage("/login")
                        .failureHandler((request, response, ex) -> {
                            String username = request.getParameter("username"); // email
                            log.warn("Failed login attempt for user={}", username);
                            response.sendRedirect("/login?error");
                        })

                        .successHandler((request, response, authentication) -> {
                            boolean isAdmin = authentication.getAuthorities().stream()
                                    .anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN"));

                            if (isAdmin) {
                                response.sendRedirect("/admin/hello");
                            } else {
                                response.sendRedirect("/user/hello");
                            }
                        })
                        .permitAll()
                )

                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/login?logout")
                        .invalidateHttpSession(true)
                        .clearAuthentication(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                // session hardening
                .sessionManagement(sm -> sm
                        .sessionFixation(sf -> sf.migrateSession())
                )

                //Referrer-Policy
                .headers(headers -> headers
                        .contentTypeOptions(cto -> {
                        }) // X-Content-Type-Options: nosniff
                        .frameOptions(fo -> fo.deny()) // X-Frame-Options: DENY
                        .referrerPolicy(rp -> rp.policy(
                                org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter.ReferrerPolicy.NO_REFERRER
                        ))
                        .contentSecurityPolicy(csp -> csp.policyDirectives(
                                "default-src 'self'; " +
                                        "style-src 'self' 'unsafe-inline'; " +
                                        "script-src 'self'; " +
                                        "img-src 'self' data:; " +
                                        "object-src 'none'; " +
                                        "base-uri 'self'; " +
                                        "frame-ancestors 'none'"
                        ))
                        .cacheControl(cache -> {
                        })   // Cache-Control
                )
                .exceptionHandling(ex -> ex
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("403 Forbidden: path={}, user={}",
                                    request.getRequestURI(),
                                    request.getUserPrincipal() != null ? request.getUserPrincipal().getName() : "anonymous");
                            response.sendRedirect("/access-denied"); // или просто response.sendError(403)
                        })
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("401 Unauthorized: path={}, ip={}", request.getRequestURI(), request.getRemoteAddr());
                            response.sendRedirect("/login");
                        })
                )

                .build();
    }

    @Bean
    public RateLimitFilter rateLimitFilter() {
        return new RateLimitFilter();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

}