package com.example.ecommerce.ecommerce.Repository;

import com.example.ecommerce.ecommerce.Entity.Users;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserRepository extends JpaRepository<Users,Long> {

    Optional<Users> findByUsername(String username);

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<Users> findByEmailOrUsername(String email, String username);
}
