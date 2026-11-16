package com.example.jobportal.repository;

import com.example.jobportal.entity.Company;
import com.example.jobportal.entity.Job;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface JobRepository extends JpaRepository<Job, Long> {

    // "Give me this company's jobs, newest first". Spring writes the SQL from the method name.
    List<Job> findByCompanyOrderByPostedAtDesc(Company company);
}