package org.pgno20.medimart.repository;

import org.pgno20.medimart.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);
    boolean existsByEmail(String email);
    Optional<User> findByEmailAndActiveTrue(String email);
    boolean existsByEmailAndActiveTrue(String email);
}
