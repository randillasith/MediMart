package org.pgno20.medimart.service;

import org.pgno20.medimart.dto.LoginDTO;
import org.pgno20.medimart.dto.UserRegistrationDTO;
import org.pgno20.medimart.dto.UserUpdateDTO;
import org.pgno20.medimart.dto.PasswordUpdateDTO;
import org.pgno20.medimart.model.User;
import org.pgno20.medimart.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@Transactional
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public User registerUser(UserRegistrationDTO dto) {
        if (!dto.getPassword().equals(dto.getConfirmPassword())) {
            throw new IllegalArgumentException("Passwords do not match");
        }

        validatePasswordStrength(dto.getPassword());

        if (userRepository.existsByEmail(dto.getEmail())) {
            throw new IllegalArgumentException("Email already in use");
        }

        User user = new User();
        user.setFullName(dto.getFullName());
        user.setEmail(dto.getEmail());
        user.setDob(dto.getDob());
        user.setGender(dto.getGender());
        user.setPassword(passwordEncoder.encode(dto.getPassword()));  // BCrypt
        user.setRoleName("ROLE_USER");
        user.setActive(true);

        return userRepository.save(user);
    }

    public User loginUser(LoginDTO dto) {
        Optional<User> userOpt = userRepository.findByEmailAndActiveTrue(dto.getEmail());
        if (userOpt.isPresent()) {
            User user = userOpt.get();
            // BCrypt-aware comparison — handles salted hashes correctly
            if (passwordEncoder.matches(dto.getPassword(), user.getPassword())) {
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
     * Get all non-admin (staff/user role) accounts, no pagination.
     * Used by the admin staff management panel.
     */
    public List<User> getAllStaff() {
        return userRepository.findAll().stream()
                .filter(u -> !"ROLE_ADMIN".equals(u.getRoleName()))
                .toList();
    }

    /**
     * Get all admin accounts.
     * Used by the admin panel to display admin list.
     */
    public List<User> getAllAdmins() {
        return userRepository.findAll().stream()
                .filter(u -> "ROLE_ADMIN".equals(u.getRoleName()) && !"superadmin@medimart.com".equals(u.getEmail()))
                .toList();
    }

    /**
     * Search users by name or email with pagination (admin use).
     */
    public Page<User> searchUsers(String search, Pageable pageable) {
        String searchTerm = (search != null && !search.isBlank()) ? search : null;
        return userRepository.searchUsers(searchTerm, pageable);
    }

    /**
     * Admin creates a new staff or admin account.
     * Validates password strength, checks email uniqueness, enforces valid roles.
     */
    public User createStaff(String fullName, String email, String rawPassword,
                            java.time.LocalDate dob, String gender, String roleName) {
        validatePasswordStrength(rawPassword);

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already in use");
        }

        String safeRole = resolveRole(roleName);

        User user = new User();
        user.setFullName(fullName);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(rawPassword));
        user.setDob(dob);
        user.setGender(gender);
        user.setRoleName(safeRole);
        user.setActive(true);

        return userRepository.save(user);
    }

    /**
     * Admin updates an existing staff account.
     * Password is only re-hashed if a non-blank new password is supplied.
     */
    public User updateStaff(Long id, String fullName, String email,
                            java.time.LocalDate dob, String gender,
                            String roleName, Boolean active, String rawPassword) {

        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));

        if ("superadmin@medimart.com".equals(user.getEmail())) {
            throw new IllegalArgumentException("The Super Admin profile cannot be updated via staff management");
        }

        user.setFullName(fullName);
        user.setEmail(email);
        user.setDob(dob);
        user.setGender(gender);

        if (roleName != null && !roleName.isBlank()) {
            user.setRoleName(resolveRole(roleName));
        }
        if (active != null) {
            user.setActive(active);
        }
        // Only re-hash and update if caller provided a new password
        if (rawPassword != null && !rawPassword.isBlank()) {
            validatePasswordStrength(rawPassword);
            user.setPassword(passwordEncoder.encode(rawPassword));
        }

        return userRepository.save(user);
    }

    public User updateUser(Long id, UserUpdateDTO dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setFullName(dto.getFullName());
        user.setDob(dto.getDob());
        user.setGender(dto.getGender());
        return userRepository.save(user);
    }

    /** Saves the shipping address entered during checkout to the user's profile. */
    public void updateShippingAddress(Long id, String address) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setShippingAddress(address);
        userRepository.save(user);
    }

    /**
     * Update a user's role (admin use).
     * Valid roles: ROLE_USER, ROLE_ADMIN
     */
    public User updateUserRole(Long id, String newRole) {
        String safeRole = resolveRole(newRole);
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setRoleName(safeRole);
        return userRepository.save(user);
    }

    public void updatePassword(Long id, PasswordUpdateDTO dto) {
        if (!dto.getNewPassword().equals(dto.getConfirmNewPassword())) {
            throw new IllegalArgumentException("New passwords do not match");
        }

        validatePasswordStrength(dto.getNewPassword());

        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));

        // BCrypt-aware current password check
        if (!passwordEncoder.matches(dto.getCurrentPassword(), user.getPassword())) {
            throw new IllegalArgumentException("Incorrect current password");
        }

        user.setPassword(passwordEncoder.encode(dto.getNewPassword()));
        userRepository.save(user);
    }

    /**
     * Soft delete — sets active = false.
     * User data is preserved but they can no longer log in.
     */
    public void deleteUser(Long id) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if ("superadmin@medimart.com".equals(user.getEmail())) {
            throw new IllegalArgumentException("The Super Admin user cannot be deleted");
        }
        user.setActive(false);
        userRepository.save(user);
    }

    /**
     * Validates password meets minimum strength requirements.
     */
    public void validatePasswordStrength(String password) {
        if (password == null || password.length() < 8) {
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

    /**
     * Validates and normalises a role string.
     * Accepted roles: ROLE_USER, ROLE_ADMIN, ROLE_SUPPLIER, ROLE_STOCK_MANAGER, ROLE_ORDER_MANAGER, ROLE_SUPPLIER_HANDLER
     */
    private String resolveRole(String roleName) {
        if (roleName == null || roleName.isBlank()) return "ROLE_USER";
        if (java.util.Set.of("ROLE_USER", "ROLE_ADMIN", "ROLE_SUPPLIER", "ROLE_STOCK_MANAGER", "ROLE_ORDER_MANAGER", "ROLE_SUPPLIER_HANDLER").contains(roleName)) {
            return roleName;
        }
        throw new IllegalArgumentException("Invalid role. Must be one of the permitted roles.");
    }
}
