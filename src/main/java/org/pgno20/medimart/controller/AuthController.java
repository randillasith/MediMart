package org.pgno20.medimart.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.pgno20.medimart.dto.AuthResponse;
import org.pgno20.medimart.dto.LoginDTO;
import org.pgno20.medimart.dto.UserRegistrationDTO;
import org.pgno20.medimart.dto.UserUpdateDTO;
import org.pgno20.medimart.dto.PasswordUpdateDTO;
import org.pgno20.medimart.model.User;
import org.pgno20.medimart.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final UserService userService;
    private final SecurityContextRepository securityContextRepository;

    public AuthController(UserService userService,
                          SecurityContextRepository securityContextRepository) {
        this.userService = userService;
        this.securityContextRepository = securityContextRepository;
    }

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody UserRegistrationDTO dto) {
        try {
            userService.registerUser(dto);
            return ResponseEntity.ok(new AuthResponse("Registration successful", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse(e.getMessage(), false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AuthResponse("An error occurred during registration", false));
        }
    }

    /**
     * Login endpoint.
     *
     * After the existing manual credential check, we also publish the
     * Authentication to Spring Security's SecurityContext so that the
     * framework enforces role-based access on subsequent requests.
     */
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginDTO dto,
                                              HttpServletRequest request,
                                              HttpServletResponse response) {
        try {
            User user = userService.loginUser(dto);

            // ── Legacy session attributes (kept for ViewController guards) ──
            HttpSession session = request.getSession();
            session.setAttribute("userId",   user.getId());
            session.setAttribute("userRole", user.getRoleName());
            session.setAttribute("userFullName", user.getFullName());
            
            String initials = "U";
            if (user.getFullName() != null && !user.getFullName().isBlank()) {
                String[] parts = user.getFullName().trim().split("\\s+");
                if (parts.length >= 2) {
                    initials = parts[0].substring(0, 1).toUpperCase() + parts[parts.length - 1].substring(0, 1).toUpperCase();
                } else {
                    initials = parts[0].substring(0, 1).toUpperCase();
                    if (parts[0].length() > 1) {
                        initials += parts[0].substring(1, 2).toUpperCase();
                    }
                }
            }
            session.setAttribute("userInitials", initials);

            // Remember Me: extend session lifetime
            if (dto.isRememberMe()) {
                session.setMaxInactiveInterval(30 * 24 * 60 * 60); // 30 days
            } else {
                session.setMaxInactiveInterval(30 * 60); // 30 minutes
            }

            // ── Spring Security: set the authenticated principal ─────────
            // This populates SecurityContextHolder so Spring Security's
            // authorization filters know this request is authenticated.
            Authentication auth = new UsernamePasswordAuthenticationToken(
                    user.getEmail(),
                    null,  // credentials null after authentication
                    List.of(new SimpleGrantedAuthority(user.getRoleName()))
            );
            SecurityContext context = SecurityContextHolder.createEmptyContext();
            context.setAuthentication(auth);
            SecurityContextHolder.setContext(context);

            // Persist the SecurityContext into the HttpSession so it survives
            // across subsequent requests (Spring Security reads it from the session).
            securityContextRepository.saveContext(context, request, response);

            return ResponseEntity.ok(new AuthResponse("Login successful", true));

        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(e.getMessage(), false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AuthResponse("An error occurred during login", false));
        }
    }

    /**
     * Logout endpoint.
     * Clears both the legacy HttpSession attributes AND the Spring SecurityContext.
     */
    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletRequest request, HttpServletResponse response) {
        // Clear Spring Security context first
        SecurityContextHolder.clearContext();

        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(new AuthResponse("Logout successful", true));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, Object> result = new HashMap<>();
        if (session != null && session.getAttribute("userId") != null) {
            Long userId = (Long) session.getAttribute("userId");
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Return only safe fields — never serialize the whole entity
                Map<String, Object> safeUser = new HashMap<>();
                safeUser.put("id",       user.getId());
                safeUser.put("fullName", user.getFullName());
                safeUser.put("email",    user.getEmail());
                safeUser.put("dob",      user.getDob());
                safeUser.put("gender",   user.getGender());
                safeUser.put("roleName", user.getRoleName());
                safeUser.put("active",   user.getActive());

                result.put("loggedIn", true);
                result.put("user", safeUser);
                return ResponseEntity.ok(result);
            }
        }
        result.put("loggedIn", false);
        return ResponseEntity.ok(result);
    }

    @PutMapping("/me")
    public ResponseEntity<AuthResponse> updateProfile(@Valid @RequestBody UserUpdateDTO dto,
                                                       HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Not logged in", false));
        }
        try {
            Long userId = (Long) session.getAttribute("userId");
            userService.updateUser(userId, dto);
            return ResponseEntity.ok(new AuthResponse("Profile updated successfully", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse(e.getMessage(), false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AuthResponse("An error occurred during profile update", false));
        }
    }

    @PutMapping("/me/password")
    public ResponseEntity<AuthResponse> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto,
                                                        HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Not logged in", false));
        }
        try {
            Long userId = (Long) session.getAttribute("userId");
            userService.updatePassword(userId, dto);
            return ResponseEntity.ok(new AuthResponse("Password updated successfully", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse(e.getMessage(), false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AuthResponse("An error occurred during password update", false));
        }
    }

    @DeleteMapping("/me")
    public ResponseEntity<AuthResponse> deleteAccount(HttpServletRequest request, HttpServletResponse response) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Not logged in", false));
        }
        try {
            Long userId = (Long) session.getAttribute("userId");
            userService.deleteUser(userId);

            // Clear Spring Security context, then invalidate session
            SecurityContextHolder.clearContext();
            session.invalidate();

            return ResponseEntity.ok(new AuthResponse("Account deleted successfully", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse(e.getMessage(), false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AuthResponse("An error occurred during account deletion", false));
        }
    }
}
