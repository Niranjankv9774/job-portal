package com.example.jobportal.service;

import com.example.jobportal.dto.RegisterDto;
import com.example.jobportal.entity.Role;
import com.example.jobportal.entity.User;
import com.example.jobportal.exception.BusinessException;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public User register(RegisterDto dto) {
        if (dto.getRole() == Role.ADMIN) {
            throw new BusinessException("Invalid role selected");
        }
        String email = dto.getEmail().trim().toLowerCase();
        if (userRepository.existsByEmail(email)) {
            throw new BusinessException("This email is already registered");
        }
        User user = new User();
        user.setName(dto.getName().trim());
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(dto.getPassword()));
        user.setRole(dto.getRole());
        return userRepository.save(user);
    }

    // NEW: find the logged-in user. Principal.getName() gives us their email.
    public User getByEmail(String email) {
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));
    }
}