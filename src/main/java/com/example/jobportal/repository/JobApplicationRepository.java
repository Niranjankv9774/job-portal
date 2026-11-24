package com.example.jobportal.repository;

import com.example.jobportal.entity.Job;
import com.example.jobportal.entity.JobApplication;
import com.example.jobportal.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobApplicationRepository extends JpaRepository<JobApplication, Long> {

    boolean existsByJobAndSeeker(Job job, User seeker);

    List<JobApplication> findBySeekerOrderByAppliedAtDesc(User seeker);

    // NEW: everyone who applied to one job, newest first
    List<JobApplication> findByJobOrderByAppliedAtDesc(Job job);
}