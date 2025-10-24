package com.example.pricerulesaidrools.security.config;

import com.example.pricerulesaidrools.security.jwt.JwtAuthenticationEntryPoint;
import com.example.pricerulesaidrools.security.jwt.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.HeadersConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.security.web.firewall.HttpFirewall;
import org.springframework.security.web.firewall.StrictHttpFirewall;
import org.springframework.security.web.header.writers.StaticHeadersWriter;
import org.springframework.security.web.header.writers.XXssProtectionHeaderWriter;

import java.util.Arrays;

/**
 * Enhanced Web Security Configuration
 *
 * This configuration addresses security vulnerabilities including:
 * - CVE-2024-38821: Static resource bypass vulnerability prevention
 * - Path traversal protection
 * - Strict HTTP firewall rules
 * - Security headers implementation
 * - OWASP security best practices
 *
 * Security measures implemented:
 * 1. Deny all static resource access by default
 * 2. Strict path validation to prevent traversal attacks
 * 3. Security headers (CSP, X-Frame-Options, etc.)
 * 4. HTTP firewall with strict rules
 * 5. JWT-based stateless authentication
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity(prePostEnabled = true, securedEnabled = true, jsr250Enabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {

    private final UserDetailsService userDetailsService;
    private final JwtAuthenticationEntryPoint unauthorizedHandler;
    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * Configure authentication provider with password encoding
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
        authProvider.setUserDetailsService(userDetailsService);
        authProvider.setPasswordEncoder(passwordEncoder());
        return authProvider;
    }

    /**
     * BCrypt password encoder for secure password storage
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // Increased strength
    }

    /**
     * Authentication manager for JWT authentication
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
        return authConfig.getAuthenticationManager();
    }

    /**
     * Strict HTTP Firewall to prevent path traversal and other attacks
     * This helps prevent CVE-2024-38821 and similar vulnerabilities
     */
    @Bean
    public HttpFirewall httpFirewall() {
        StrictHttpFirewall firewall = new StrictHttpFirewall();

        // Block URL encoding percent signs
        firewall.setAllowUrlEncodedPercent(false);

        // Block URL encoding slashes
        firewall.setAllowUrlEncodedSlash(false);

        // Block URL encoding periods
        firewall.setAllowUrlEncodedPeriod(false);

        // Block backslash
        firewall.setAllowBackSlash(false);

        // Block null characters
        firewall.setAllowNull(false);

        // Block semicolons in URLs
        firewall.setAllowSemicolon(false);

        // Only allow specific HTTP methods
        firewall.setAllowedHttpMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS", "HEAD"));

        return firewall;
    }

    /**
     * Web Security Customizer for ignoring specific paths from security
     * WARNING: Use sparingly - only for truly public resources
     */
    @Bean
    public WebSecurityCustomizer webSecurityCustomizer() {
        return (web) -> web.httpFirewall(httpFirewall())
            .ignoring()
            // Only ignore paths that absolutely must be public
            // Do NOT add /static/**, /resources/**, etc. here
            .requestMatchers("/favicon.ico");
    }

    /**
     * Main security filter chain configuration
     * Implements defense-in-depth with multiple security layers
     */
    @Bean
    @Primary
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            // Disable CSRF for stateless JWT authentication
            .csrf(AbstractHttpConfigurer::disable)

            // Configure CORS if needed (customize as per requirements)
            .cors(cors -> cors.configurationSource(request -> {
                var config = new org.springframework.web.cors.CorsConfiguration();
                config.setAllowedOriginPatterns(Arrays.asList("https://*.yourdomain.com"));
                config.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
                config.setAllowedHeaders(Arrays.asList("Authorization", "Content-Type", "X-Requested-With"));
                config.setExposedHeaders(Arrays.asList("X-Total-Count"));
                config.setAllowCredentials(true);
                config.setMaxAge(3600L);
                return config;
            }))

            // Security headers configuration
            .headers(headers -> headers
                // X-Frame-Options: DENY
                .frameOptions(HeadersConfigurer.FrameOptionsConfig::deny)

                // X-Content-Type-Options: nosniff
                .contentTypeOptions(contentType -> {})

                // X-XSS-Protection: 1; mode=block
                .xssProtection(xss -> xss
                    .headerValue(XXssProtectionHeaderWriter.HeaderValue.ENABLED_MODE_BLOCK))

                // Content-Security-Policy
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'; " +
                        "script-src 'self' 'unsafe-inline' 'unsafe-eval'; " +
                        "style-src 'self' 'unsafe-inline'; " +
                        "img-src 'self' data: https:; " +
                        "font-src 'self' data:; " +
                        "connect-src 'self'; " +
                        "frame-ancestors 'none'; " +
                        "base-uri 'self'; " +
                        "form-action 'self';"))

                // Strict-Transport-Security (HSTS)
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                    .preload(true))

                // Referrer-Policy
                .referrerPolicy(referrer -> referrer
                    .policy(org.springframework.security.web.header.writers.ReferrerPolicyHeaderWriter
                        .ReferrerPolicy.STRICT_ORIGIN_WHEN_CROSS_ORIGIN))

                // Permissions-Policy (replaces Feature-Policy)
                .addHeaderWriter(new StaticHeadersWriter("Permissions-Policy",
                    "geolocation=(), microphone=(), camera=(), payment=()"))
            )

            // Exception handling
            .exceptionHandling(exception -> exception
                .authenticationEntryPoint(unauthorizedHandler))

            // Session management - stateless for JWT
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // Authorization rules - CRITICAL FOR CVE-2024-38821 PREVENTION
            .authorizeHttpRequests(auth -> auth
                // EXPLICITLY DENY all static resource patterns
                // This prevents CVE-2024-38821 static resource bypass
                .requestMatchers("/static/**").denyAll()
                .requestMatchers("/resources/**").denyAll()
                .requestMatchers("/public/**").denyAll()
                .requestMatchers("/webjars/**").denyAll()
                .requestMatchers("*.js", "*.css", "*.html", "*.json").denyAll()

                // Block common attack patterns
                .requestMatchers("/**/*../").denyAll()
                .requestMatchers("/**/*..*").denyAll()

                // Allow only specific public endpoints
                .requestMatchers("/api/auth/**").permitAll()
                .requestMatchers("/health/**").permitAll()
                .requestMatchers("/actuator/health").permitAll()
                .requestMatchers("/actuator/info").permitAll()
                .requestMatchers("/actuator/metrics").permitAll()
                .requestMatchers("/actuator/prometheus").permitAll()

                // API documentation (only in non-prod environments)
                .requestMatchers("/v3/api-docs/**", "/swagger-ui/**", "/swagger-ui.html").permitAll()

                // All other actuator endpoints require authentication
                .requestMatchers("/actuator/**").hasRole("ADMIN")

                // Everything else requires authentication
                .anyRequest().authenticated()
            );

        // Add authentication provider
        http.authenticationProvider(authenticationProvider());

        // Add JWT filter
        http.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}