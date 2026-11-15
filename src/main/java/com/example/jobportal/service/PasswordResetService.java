package com.example.jobportal.service;

import com.example.jobportal.entity.PasswordResetToken;
import com.example.jobportal.entity.User;
import com.example.jobportal.exception.BusinessException;
import com.example.jobportal.repository.PasswordResetTokenRepository;
import com.example.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private static final int EXPIRY_MINUTES = 15;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    @Value("${app.base-url:http://localhost:8080}")
    private String baseUrl;

    @Value("${app.mail-from:noreply@jobportal.local}")
    private String mailFrom;

    /**
     * Step 1: create a token and email the link.
     * Does nothing if the email is unknown, so nobody can discover which emails exist.
     */
    @Transactional
    public void createAndSend(String email) {
        if (email == null || email.isBlank()) {
            return;
        }
        userRepository.findByEmail(email.trim().toLowerCase()).ifPresent(user -> {
            tokenRepository.deleteByUser(user);                    // only one active link per user

            PasswordResetToken token = new PasswordResetToken();
            token.setToken(UUID.randomUUID().toString());          // random and hard to guess
            token.setUser(user);
            token.setExpiryTime(LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
            tokenRepository.save(token);

            sendEmail(user, token.getToken());
        });
    }

    /** Step 2: is this token usable (exists, not used, not expired)? */
    public boolean isValid(String token) {
        if (token == null) {
            return false;
        }
        return tokenRepository.findByToken(token)
                .map(t -> !t.isUsed() && t.getExpiryTime().isAfter(LocalDateTime.now()))
                .orElse(false);
    }

    /** Step 3: set the new password and burn the token. */
    @Transactional
    public void resetPassword(String tokenValue, String newPassword) {
        PasswordResetToken token = tokenRepository.findByToken(tokenValue)
                .orElseThrow(() -> new BusinessException("Invalid or expired reset link"));
        if (token.isUsed() || token.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new BusinessException("Invalid or expired reset link");
        }
        User user = token.getUser();
        user.setPassword(passwordEncoder.encode(newPassword));     // hash the new password
        userRepository.save(user);
        token.setUsed(true);                                       // the link cannot be used again
        tokenRepository.save(token);
    }

    private void sendEmail(User user, String token) {
        String link = baseUrl + "/reset-password?token=" + token;

        // DEV ONLY: prints the link in the console so you can test even if email is not set up.
        // Delete this line on Day 15, because real links should never go in logs.
        log.info("DEV ONLY reset link for {}: {}", user.getEmail(), link);

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(mailFrom);
        message.setTo(user.getEmail());
        message.setSubject("Reset your Job Portal password");
        message.setText("Hi " + user.getName() + ",\n\n"
                + "Click the link below to choose a new password. It expires in "
                + EXPIRY_MINUTES + " minutes.\n\n"
                + link + "\n\n"
                + "If you did not ask for this, you can ignore this email.");
        try {
            mailSender.send(message);
        } catch (MailException e) {
            log.error("Could not send reset email to {}: {}", user.getEmail(), e.getMessage());
        }
    }
}