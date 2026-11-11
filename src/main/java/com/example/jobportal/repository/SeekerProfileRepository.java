package com.example.jobportal.repository;

import com.example.jobportal.entity.SeekerProfile;
import com.example.jobportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface SeekerProfileRepository extends JpaRepository<SeekerProfile, Long> {

    Optional<SeekerProfile> findByUser(User user);
}