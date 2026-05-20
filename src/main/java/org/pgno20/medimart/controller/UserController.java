package org.pgno20.medimart.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.pgno20.medimart.model.User;
import org.pgno20.medimart.service.UserService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Admin-facing REST controller for staff / user account management.
 *
 * All operations are routed through UserService to ensure:
 *  - Password strength validation before hashing with BCrypt
 *  - Email uniqueness enforced with a clean 400 (not a DB 500)
 *  - Role injection prevention (only valid system roles accepted)
 *  - Consistent soft-delete behaviour (active = false, not hard delete)
 *
 * Access is restricted to ROLE_ADMIN by SecurityConfig.
 */
@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Inner request DTOs  (kept local — no need for separate files)
    // ──────────────────────────────────────────────────────────────────────────

    /** Payload for creating a new staff / admin account. */
    static class StaffCreateRequest {
        @NotBlank(message = "Full name is required")
        public String fullName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        public String email;

        @NotBlank(message = "Password is required")
        @Size(min = 8, message = "Password must be at least 8 characters")
        public String password;

        @NotNull(message = "Date of birth is required")
        public LocalDate dob;

        @NotBlank(message = "Gender is required")
        public String gender;

        public String roleName;   // defaults to ROLE_USER if blank
    }

    /** Payload for updating an existing account. Password is optional. */
    static class StaffUpdateRequest {
        @NotBlank(message = "Full name is required")
        public String fullName;

        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        public String email;

        @NotNull(message = "Date of birth is required")
        public LocalDate dob;

        @NotBlank(message = "Gender is required")
        public String gender;

        public String roleName;   // ROLE_USER, ROLE_ADMIN, ROLE_STOCK_MANAGER, ROLE_ORDER_MANAGER, or ROLE_SUPPLIER_HANDLER
        public Boolean active;    // null = don't change

        /** Leave blank to keep the existing password unchanged. */
        public String password;
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Endpoints
    // ──────────────────────────────────────────────────────────────────────────

    /** GET /api/users — all non-admin accounts (staff list in admin panel). */
    @GetMapping
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        List<Map<String, Object>> staff = userService.getAllStaff().stream()
                .map(this::toSafeMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(staff);
    }

    /** GET /api/users/admins — all admin accounts. */
    @GetMapping("/admins")
    public ResponseEntity<List<Map<String, Object>>> getAllAdmins() {
        List<Map<String, Object>> admins = userService.getAllAdmins().stream()
                .map(this::toSafeMap)
                .collect(Collectors.toList());
        return ResponseEntity.ok(admins);
    }

    /** POST /api/users — create a new staff or admin account. */
    @PostMapping
    public ResponseEntity<?> createStaff(@Valid @RequestBody StaffCreateRequest req) {
        try {
            User saved = userService.createStaff(
                    req.fullName, req.email, req.password,
                    req.dob, req.gender, req.roleName
            );
            return ResponseEntity.status(HttpStatus.CREATED).body(toSafeMap(saved));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Could not create user"));
        }
    }

    /** PUT /api/users/{id} — update an existing staff member. */
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStaff(@PathVariable Long id,
                                          @Valid @RequestBody StaffUpdateRequest req) {
        try {
            User updated = userService.updateStaff(
                    id, req.fullName, req.email,
                    req.dob, req.gender,
                    req.roleName, req.active, req.password
            );
            return ResponseEntity.ok(toSafeMap(updated));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Could not update user"));
        }
    }

    /**
     * DELETE /api/users/{id} — soft delete (sets active = false).
     * The account record is preserved for audit purposes; the user
     * simply cannot log in any more.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userService.deleteUser(id);
            return ResponseEntity.ok(Map.of("message", "User deactivated successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("message", "Could not delete user"));
        }
    }

    // ──────────────────────────────────────────────────────────────────────────
    //  Helpers
    // ──────────────────────────────────────────────────────────────────────────

    /**
     * Converts a User entity to a safe response map.
     * The password hash is NEVER included in API responses.
     */
    private Map<String, Object> toSafeMap(User user) {
        return Map.of(
                "id",       user.getId(),
                "fullName", user.getFullName(),
                "email",    user.getEmail(),
                "dob",      user.getDob() != null ? user.getDob().toString() : "",
                "gender",   user.getGender(),
                "roleName", user.getRoleName(),
                "active",   user.getActive()
        );
    }
}