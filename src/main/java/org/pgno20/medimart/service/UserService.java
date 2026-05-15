package org.pgno20.medimart.service;

import org.pgno20.medimart.dto.LoginDTO;
import org.pgno20.medimart.dto.UserRegistrationDTO;
import org.pgno20.medimart.dto.UserUpdateDTO;
import org.pgno20.medimart.dto.PasswordUpdateDTO;
import org.pgno20.medimart.model.User;
import org.pgno20.medimart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.nio.charset.StandardCharsets;

@Service
@Transactional
public class UserService {

    @Autowired
    private UserRepository userRepository;

    public User registerUser(UserRegistrationDTO dto) {
        // Confirm password check
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        // Password strength validation
        validatePasswordStrength(dto.getPassword());

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setDob(dto.getDob());
        user.setGender(dto.getGender());
        // Store password as SHA-256 hash
        user.setPassword(hashPassword(dto.getPassword())); 
        user.setRoleName("ROLE_USER");
        user.setActive(true);

        return userRepository.save(user);
    }

    public User loginUser(LoginDTO dto) {
        // Only allow active users to log in
        Optional<User> userOpt = userRepository.findByEmailAndActiveTrue(dto.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            if (user.getPassword().equals(hashPassword(dto.getPassword()))) {
                return user;
            }
        }
        throw new IllegalArgumentException("Invalid email or password");
    }

    public Optional<User> getUserById(Long id) {
        return userRepository.findById(id);
    }

    /**
     * Get all users with pagination (admin use).
     */
    public Page<User> getAllUsers(Pageable pageable) {
        return userRepository.findAll(pageable);
    }

    /**
     * Search users by name or email with pagination (admin use).
     */
    public Page<User> searchUsers(String search, Pageable pageable) {
        String searchTerm = (search != null && !search.isBlank()) ? search : null;
        return userRepository.searchUsers(searchTerm, pageable);
    }

    public User updateUser(Long id, UserUpdateDTO dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setFullName(dto.getFullName());
        user.setDob(dto.getDob());
        user.setGender(dto.getGender());
        return userRepository.save(user);
    }

    /**
     * Update a user's role (admin use).
     * Valid roles: ROLE_USER, ROLE_ADMIN
     */
    public User updateUserRole(Long id, String newRole) {
        if (!"ROLE_USER".equals(newRole) && !"ROLE_ADMIN".equals(newRole)) {
            throw new IllegalArgumentException("Invalid role. Must be ROLE_USER or ROLE_ADMIN");
        }
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRoleName(newRole);
        return userRepository.save(user);
    }

    public void updatePassword(Long id, PasswordUpdateDTO dto) {
        // Confirm new password check
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }

        // Password strength validation
        validatePasswordStrength(dto.getNewPassword());

        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!user.getPassword().equals(hashPassword(dto.getCurrentPassword()))) {
            throw new IllegalArgumentException("Incorrect current password");
        }
        user.setPassword(hashPassword(dto.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Soft delete - sets active = false instead of removing from database.
     * User data is preserved but they can no longer log in.
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setActive(false);
        userRepository.save(user);
    }

    /**
     * Validates password meets strength requirements:
     * - At least 8 characters
     * - Contains at least one uppercase letter
     * - Contains at least one lowercase letter
     * - Contains at least one digit
     */
    void validatePasswordStrength(String password) {
        if (password.length() < 8) {
            throw new IllegalArgumentException("Password must be at least 8 characters long");
        }
        if (!password.matches(".*[A-Z].*")) {
            throw new IllegalArgumentException("Password must contain at least one uppercase letter");
        }
        if (!password.matches(".*[a-z].*")) {
            throw new IllegalArgumentException("Password must contain at least one lowercase letter");
        }
        if (!password.matches(".*\\d.*")) {
            throw new IllegalArgumentException("Password must contain at least one number");
        }
    }

    String hashPassword(String password) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedhash = digest.digest(password.getBytes(StandardCharsets.UTF_8));
            StringBuilder hexString = new StringBuilder(2 * encodedhash.length);
            for (byte b : encodedhash) {
                String hex = Integer.toHexString(0xff & b);
                if (hex.length() == 1) {
                    hexString.append('0');
                }
                hexString.append(hex);
            }
            return hexString.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Failed to hash password", e);
        }
    }
}
