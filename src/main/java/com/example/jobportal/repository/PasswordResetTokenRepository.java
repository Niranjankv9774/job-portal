package com.example.jobportal.repository;

import com.example.jobportal.entity.PasswordResetToken;
import com.example.jobportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface PasswordResetTokenRepository extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByToken(String token);

    void deleteByUser(User user);         // remove a user's old tokens before making a new one
}