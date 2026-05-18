package org.pgno20.medimart.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;

/**
 * Central Spring Security configuration for MediMart.
 *
 * Authentication strategy: Session-based (HttpSession).
 * The existing custom login flow in AuthController is preserved —
 * on successful credential check, the AuthController programmatically
 * sets the SecurityContext, which is then persisted to the session
 * by Spring Security's HttpSessionSecurityContextRepository.
 *
 * CSRF: Disabled for /api/** paths because the frontend is a
 * fetch()-based SPA that sends JSON (not browser form submissions).
 * Page routes keep standard protection via Spring Security defaults.
 *
 * Role hierarchy:
 *   ROLE_ADMIN  — full access to all admin APIs and pages
 *   ROLE_USER   — access to own profile endpoints
 *   (anonymous) — public storefront, login, register
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final MediMartUserDetailsService userDetailsService;

    public SecurityConfig(MediMartUserDetailsService userDetailsService) {
        this.userDetailsService = userDetailsService;
    }

    // ──────────────────────────────────────────────
    //  Beans
    // ──────────────────────────────────────────────

    /**
     * BCrypt is a salted, adaptive hashing algorithm.
     * Strength 12 = ~250ms per hash (strong against brute-force).
     * Each encoded password includes a random salt, so two identical
     * passwords always produce different hashes — rainbow tables useless.
     *
     * ⚠ NOTE: Existing SHA-256 hashed passwords in the DB are now invalid.
     * Users must reset their passwords, or the DB must be re-seeded.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }

    /**
     * DaoAuthenticationProvider wires our UserDetailsService + PasswordEncoder
     * so that Spring Security's AuthenticationManager can verify credentials.
     * AuthController uses this manager programmatically on login.
     *
     * NOTE: Spring Security 6.4+ (Spring Boot 4) requires UserDetailsService
     * to be passed via the constructor — the no-arg constructor was removed.
     */
    @Bean
    public DaoAuthenticationProvider authenticationProvider() {
        // Spring Security 6.4+: constructor takes UserDetailsService directly
        DaoAuthenticationProvider provider = new DaoAuthenticationProvider(userDetailsService);
        provider.setPasswordEncoder(passwordEncoder());
        return provider;
    }

    /**
     * Exposes the AuthenticationManager bean so AuthController can inject it
     * and call authenticate() during the login flow.
     */
    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration config) throws Exception {
        return config.getAuthenticationManager();
    }

    /**
     * Saves / restores the SecurityContext from the HttpSession.
     * Injected into AuthController so it can persist the context after login.
     */
    @Bean
    public SecurityContextRepository securityContextRepository() {
        return new HttpSessionSecurityContextRepository();
    }

    // ──────────────────────────────────────────────
    //  Filter chain — Access rules
    // ──────────────────────────────────────────────

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {

        http
            // ── CSRF ──────────────────────────────────────────────────────
            // Disabled for /api/** because the frontend sends fetch() JSON
            // requests, not HTML form submissions. Page routes get default
            // CSRF protection automatically since they're not excluded.
            .csrf(csrf -> csrf
                .ignoringRequestMatchers("/api/**")
            )

            // ── Authorization rules ───────────────────────────────────────
            .authorizeHttpRequests(auth -> auth

                // ── Static resources & public pages ───────────────────────
                .requestMatchers(
                    "/",
                    "/index.html",
                    "/login.html",
                    "/register.html",
                    "/catalog.html",
                    "/checkout.html",
                    "/order-success.html",
                    "/prescription.html",
                    "/profile.html",
                    "/catalog",
                    "/home"
                ).permitAll()

                .requestMatchers(
                    "/static/**",
                    "/uploads/**",
                    "/images/**",
                    "/error/**",
                    "/favicon.ico"
                ).permitAll()

                // ── Auth API — register & login are always public ─────────
                .requestMatchers(HttpMethod.POST, "/api/auth/register", "/api/auth/login").permitAll()

                // GET /api/auth/me is public — it returns { loggedIn: false }
                // for anonymous visitors (used by the frontend to detect session)
                .requestMatchers(HttpMethod.GET, "/api/auth/me").permitAll()

                // PUT /api/auth/me/address is for logged-in customers (session-checked in controller)
                .requestMatchers(HttpMethod.PUT, "/api/auth/me/address").permitAll()

                // ── Public storefront — read-only medicine / category data ─
                .requestMatchers(HttpMethod.GET,
                    "/api/medicines",
                    "/api/medicines/storefront",
                    "/api/medicines/stats",
                    "/api/medicines/{id}",
                    "/api/categories"
                ).permitAll()

                // ── Authenticated users — own profile management ───────────
                .requestMatchers(
                    "/api/auth/logout",
                    "/api/auth/me"         // PUT / DELETE also covered
                ).authenticated()

                // ── Admin-only pages (Thymeleaf templates) ────────────────
                .requestMatchers(
                    "/medicines",
                    "/supplier-details",
                    "/users-portal",
                    "/addmindetails",
                    "/orders-management"
                ).hasAuthority("ROLE_ADMIN")

                // ── Admin-only API — write operations on medicines ─────────
                .requestMatchers(HttpMethod.POST,   "/api/medicines").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/medicines/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/medicines/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST,   "/api/medicines/*/image").hasAuthority("ROLE_ADMIN")

                // ── Admin-only API — stock batch management ────────────────
                .requestMatchers(HttpMethod.POST, "/api/medicines/*/batches").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT,  "/api/medicines/*/batches/**").hasAuthority("ROLE_ADMIN")
                // GET batches is admin-only too (internal inventory data)
                .requestMatchers(HttpMethod.GET,  "/api/medicines/*/batches").hasAuthority("ROLE_ADMIN")

                // ── Admin-only API — users, suppliers, orders, categories ──
                .requestMatchers("/api/users/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers("/api/suppliers/**").hasAuthority("ROLE_ADMIN")
                // POST /api/orders is allowed for any customer (even guests)
                .requestMatchers(HttpMethod.POST, "/api/orders").permitAll()
                // GET /api/orders is allowed for any session (profile page filters by customerName)
                .requestMatchers(HttpMethod.GET, "/api/orders").permitAll()
                // PUT/DELETE orders are admin-only
                .requestMatchers("/api/orders/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.POST,   "/api/categories").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.PUT,    "/api/categories/**").hasAuthority("ROLE_ADMIN")
                .requestMatchers(HttpMethod.DELETE, "/api/categories/**").hasAuthority("ROLE_ADMIN")

                // ── Anything else requires at least a valid session ────────
                .anyRequest().authenticated()
            )

            // ── Session management ────────────────────────────────────────
            // Use Spring Security's session fixation protection.
            // AuthController manually manages HttpSession — we allow that here.
            .sessionManagement(session -> session
                .sessionFixation().migrateSession()
            )

            // ── Disable built-in login / basic auth forms ─────────────────
            // MediMart uses a custom REST login endpoint (/api/auth/login).
            .formLogin(form -> form.disable())
            .httpBasic(basic -> basic.disable())

            // ── Unauthorised / Forbidden responses ────────────────────────
            // Return 401/403 JSON instead of redirecting to a login page,
            // because the frontend handles these responses via fetch().
            .exceptionHandling(ex -> ex
                .authenticationEntryPoint((request, response, authException) -> {
                    String accept = request.getHeader("Accept");
                    // HTML requests (page navigations) get a redirect to login
                    if (accept != null && accept.contains("text/html")) {
                        response.sendRedirect("/login.html");
                    } else {
                        // API requests get a JSON 401
                        response.setStatus(401);
                        response.setContentType("application/json");
                        response.getWriter().write(
                            "{\"error\":\"Authentication required. Please log in.\",\"success\":false}"
                        );
                    }
                })
                .accessDeniedHandler((request, response, accessDeniedException) -> {
                    String accept = request.getHeader("Accept");
                    if (accept != null && accept.contains("text/html")) {
                        response.sendRedirect("/index.html");
                    } else {
                        response.setStatus(403);
                        response.setContentType("application/json");
                        response.getWriter().write(
                            "{\"error\":\"Access denied. Admin privileges required.\",\"success\":false}"
                        );
                    }
                })
            )

            .authenticationProvider(authenticationProvider());

        return http.build();
    }
}
