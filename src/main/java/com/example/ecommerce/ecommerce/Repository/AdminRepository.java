package com.example.ecommerce.ecommerce.Repository;

import com.example.ecommerce.ecommerce.Entity.Admin;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface AdminRepository extends JpaRepository<Admin, Long> {

    boolean existsByUsername(String username);
    boolean existsByEmail(String email);

    Optional<Admin> findByEmailOrUsername(String email, String username);
}
