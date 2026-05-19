package org.pgno20.medimart.controller;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import jakarta.validation.Valid;
import org.pgno20.medimart.dto.AuthResponse;
import org.pgno20.medimart.dto.LoginDTO;
import org.pgno20.medimart.dto.UserRegistrationDTO;
import org.pgno20.medimart.dto.UserUpdateDTO;
import org.pgno20.medimart.dto.PasswordUpdateDTO;
import org.pgno20.medimart.model.User;
import org.pgno20.medimart.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.Map;
import java.util.HashMap;
import java.util.Optional;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    @Autowired
    private UserService userService;

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

    @PostMapping("/supplier-setup")
    public ResponseEntity<AuthResponse> setupSupplier(@Valid @RequestBody org.pgno20.medimart.dto.SupplierSetupDTO dto) {
        try {
            userService.setupSupplierAccount(dto);
            return ResponseEntity.ok(new AuthResponse("Supplier account configured successfully", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse(e.getMessage(), false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AuthResponse("An error occurred during supplier setup", false));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginDTO dto, HttpServletRequest request) {
        try {
            User user = userService.loginUser(dto);
            // Create session
            HttpSession session = request.getSession();
            session.setAttribute("userId", user.getId());
            session.setAttribute("userRole", user.getRole());
            
            // Remember Me: extend session to 30 days, else default 30 minutes
            if (dto.isRememberMe()) {
                session.setMaxInactiveInterval(30 * 24 * 60 * 60); // 30 days in seconds
            } else {
                session.setMaxInactiveInterval(30 * 60); // 30 minutes
            }
            
            return ResponseEntity.ok(new AuthResponse("Login successful", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse(e.getMessage(), false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AuthResponse("An error occurred during login", false));
        }
    }

    @PostMapping("/logout")
    public ResponseEntity<AuthResponse> logout(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session != null) {
            session.invalidate();
        }
        return ResponseEntity.ok(new AuthResponse("Logout successful", true));
    }

    @GetMapping("/me")
    public ResponseEntity<Map<String, Object>> getCurrentUser(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        Map<String, Object> response = new HashMap<>();
        if (session != null && session.getAttribute("userId") != null) {
            Long userId = (Long) session.getAttribute("userId");
            Optional<User> userOpt = userService.getUserById(userId);
            if (userOpt.isPresent()) {
                User user = userOpt.get();
                // Don't return the password
                user.setPassword(null);
                response.put("loggedIn", true);
                response.put("user", user);
                return ResponseEntity.ok(response);
            }
        }
        response.put("loggedIn", false);
        return ResponseEntity.ok(response);
    }

    @PutMapping("/me")
    public ResponseEntity<AuthResponse> updateProfile(@Valid @RequestBody UserUpdateDTO dto, HttpServletRequest request) {
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
    public ResponseEntity<AuthResponse> updatePassword(@Valid @RequestBody PasswordUpdateDTO dto, HttpServletRequest request) {
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
    public ResponseEntity<AuthResponse> deleteAccount(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute("userId") == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(new AuthResponse("Not logged in", false));
        }
        try {
            Long userId = (Long) session.getAttribute("userId");
            userService.deleteUser(userId);
            session.invalidate();
            return ResponseEntity.ok(new AuthResponse("Account deleted successfully", true));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse(e.getMessage(), false));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AuthResponse("An error occurred during account deletion", false));
        }
    }

    @ExceptionHandler(org.springframework.web.bind.MethodArgumentNotValidException.class)
    public ResponseEntity<AuthResponse> handleValidationExceptions(org.springframework.web.bind.MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getFieldErrors().stream()
                .map(error -> error.getDefaultMessage())
                .findFirst()
                .orElse("Validation failed");
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AuthResponse(errorMessage, false));
    }
}
