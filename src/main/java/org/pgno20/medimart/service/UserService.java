package org.pgno20.medimart.service;

import org.pgno20.medimart.dto.LoginDTO;
import org.pgno20.medimart.dto.UserRegistrationDTO;
import org.pgno20.medimart.dto.UserUpdateDTO;
import org.pgno20.medimart.dto.PasswordUpdateDTO;
import org.pgno20.medimart.model.User;
import org.pgno20.medimart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
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
        user.setRole("ROLE_USER");

        return userRepository.save(user);
    }

    public User loginUser(LoginDTO dto) {
        Optional<User> userOpt = userRepository.findByEmail(dto.getEmail());
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

    public User updateUser(Long id, UserUpdateDTO dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        user.setFullName(dto.getFullName());
        user.setDob(dto.getDob());
        user.setGender(dto.getGender());
        return userRepository.save(user);
    }

    public void updatePassword(Long id, PasswordUpdateDTO dto) {
        User user = userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("User not found"));
        if (!user.getPassword().equals(hashPassword(dto.getCurrentPassword()))) {
            throw new IllegalArgumentException("Incorrect current password");
        }
        user.setPassword(hashPassword(dto.getNewPassword()));
        userRepository.save(user);
    }

    public void deleteUser(Long id) {
        if (!userRepository.existsById(id)) {
            throw new IllegalArgumentException("User not found");
        }
        userRepository.deleteById(id);
    }

    private String hashPassword(String password) {
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
