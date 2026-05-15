package org.pgno20.medimart.controller;

import org.pgno20.medimart.model.User;
import org.pgno20.medimart.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map; // REQUIRED for Map.of to work

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserRepository userRepository;

    // GET: Load all staff into the table
       @GetMapping
        public List<User> getAllUsers() {
            return userRepository.findAll().stream()
                    .filter(user -> !"ADMIN".equals(user.getRoleName()))
                    .toList();
        }

    // POST: Save a new staff member
    @PostMapping
    public ResponseEntity<?> createStaff(@RequestBody User user) {
        try {
            // Debugging log in terminal
            System.out.println("Attempting to save: " + user.getFullName());

            if (user.getPassword() == null || user.getPassword().isEmpty()) {
                return ResponseEntity.badRequest().body(Map.of("message", "Password is required for new members"));
            }

            // Ensure status is set
            if (user.getActive() == null) {
                user.setActive(true);
            }

            User savedUser = userRepository.save(user);
            return ResponseEntity.ok(savedUser);
        } catch (Exception e) {
            e.printStackTrace(); 
            // Returning as a Map ensures the frontend receives JSON, not plain text
            return ResponseEntity.badRequest().body(Map.of("message", "Database error: " + e.getMessage()));
        }
    }

    // PUT: Update existing staff member
    @PutMapping("/{id}")
    public ResponseEntity<?> updateStaff(@PathVariable Long id, @RequestBody User userDetails) {
        return userRepository.findById(id).map(existingUser -> {
            try {
                existingUser.setFullName(userDetails.getFullName());
                existingUser.setEmail(userDetails.getEmail());
                existingUser.setDob(userDetails.getDob());
                existingUser.setGender(userDetails.getGender());
                existingUser.setRoleName(userDetails.getRoleName());
                existingUser.setActive(userDetails.getActive());

                // Logic: Only update password if a new one is typed in the form
                if (userDetails.getPassword() != null && !userDetails.getPassword().isEmpty()) {
                    existingUser.setPassword(userDetails.getPassword());
                }   

                userRepository.save(existingUser);
                return ResponseEntity.ok(Map.of("message", "Update successful"));
            } catch (Exception e) {
                return ResponseEntity.badRequest().body(Map.of("message", e.getMessage()));
            }
        }).orElse(ResponseEntity.notFound().build());
    }



    // Get = Admin only details
    @GetMapping("/admins")
public List<User> getAllAdmins() {
    // This assumes your roleName string for admins is "ADMIN"
    return userRepository.findAll().stream()
            .filter(user -> "ADMIN".equals(user.getRoleName()))
            .toList();
}

    // DELETE: Remove a staff member
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteUser(@PathVariable Long id) {
        try {
            userRepository.deleteById(id);
            return ResponseEntity.ok(Map.of("message", "Deleted successfully"));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("message", "Could not delete user"));
        }
    }
}