package com.afriasdev.cmms.security.repository;

import com.afriasdev.cmms.security.model.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    Optional<User> findByEmail(String email);

    Optional<User> findByUsername(String username);

    // Para permitir login por email o username
    default Optional<User> findByUsernameOrEmail(String usernameOrEmail) {
        if (usernameOrEmail == null) return Optional.empty();
        return findByUsername(usernameOrEmail)
                .or(() -> findByEmail(usernameOrEmail));
    }

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);
}
