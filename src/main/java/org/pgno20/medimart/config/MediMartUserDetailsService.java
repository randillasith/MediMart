package org.pgno20.medimart.config;

import org.pgno20.medimart.model.User;
import org.pgno20.medimart.repository.UserRepository;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Integrates MediMart's User entity with Spring Security's authentication
 * mechanism. The "username" in Spring Security maps to the user's email
 * address (the login identifier used throughout the application).
 *
 * This bean is used by Spring Security's DaoAuthenticationProvider and
 * also suppresses the default in-memory user auto-configuration.
 */
@Service
public class MediMartUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    public MediMartUserDetailsService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    /**
     * Loads an active user by email for Spring Security.
     * Inactive (soft-deleted) users cannot authenticate.
     *
     * @param email the login email (used as "username" in Spring Security)
     * @throws UsernameNotFoundException if the user doesn't exist or is inactive
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmailAndActiveTrue(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException("No active user found with email: " + email));

        return org.springframework.security.core.userdetails.User.builder()
                .username(user.getEmail())
                .password(user.getPassword())                          // stored SHA-256 hash
                .authorities(List.of(new SimpleGrantedAuthority(user.getRoleName()))) // ROLE_ADMIN or ROLE_USER
                .build();
    }
}
