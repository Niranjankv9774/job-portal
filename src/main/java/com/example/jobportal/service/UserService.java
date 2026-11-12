package com.example.jobportal.service;

import com.example.jobportal.dto.RegisterDto;
import com.example.jobportal.entity.Role;
import com.example.jobportal.entity.User;
import com.example.jobportal.exception.BusinessException;
import com.example.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor     // Lombok writes a constructor so Spring can hand in the two fields below
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterDto dto) {
        // Nobody can register themselves as admin
        if (dto.getRole() == Role.ADMIN) {
            throw new BusinessException("Invalid role selected");
        }

        // Emails are stored in lowercase so "A@x.com" and "a@x.com" count as the same
        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("This email is already registered");
        }

        User user = new User();
        user.setName(dto.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));   // hash it, never store the raw password
        user.setRole(dto.getRole());
        return userRepository.save(user);                              // INSERT into the users table
    }
}