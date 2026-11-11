package com.example.jobportal.repository;

import com.example.jobportal.entity.Role;
import com.example.jobportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // Spring reads the method name and writes the SQL for you
    Optional<User> findByEmail(String email);

    boolean existsByEmail(String email);

    List<User> findByRoleNotOrderByCreatedAtDesc(Role role);
}