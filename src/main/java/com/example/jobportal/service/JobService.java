package com.example.jobportal.service;

import com.example.jobportal.dto.JobForm;
import com.example.jobportal.entity.Company;
import com.example.jobportal.entity.Job;
import com.example.jobportal.entity.User;
import com.example.jobportal.exception.BusinessException;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.repository.CompanyRepository;
import com.example.jobportal.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private static final int PAGE_SIZE = 10;

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    // NEW: search open jobs, 10 per page, newest first
    public Page<Job> search(String keyword, String location, int page) {
        return jobRepository.search(
                keyword == null ? "" : keyword.trim(),
                location == null ? "" : location.trim(),
                PageRequest.of(Math.max(page, 0), PAGE_SIZE, Sort.by("postedAt").descending()));
    }

    // NEW: load one job or fail with "not found"
    public Job getById(Long id) {
        return jobRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Job not found"));
    }

    public Job createJob(User recruiter, JobForm form) {
        Company company = companyRepository.findByRecruiter(recruiter)
                .orElseThrow(() -> new BusinessException(
                        "Please create your company profile before posting jobs"));
        Job job = new Job();
        job.setCompany(company);
        copyFields(job, form);
        return jobRepository.save(job);
    }

    public List<Job> jobsOf(User recruiter) {
        return companyRepository.findByRecruiter(recruiter)
                .map(jobRepository::findByCompanyOrderByPostedAtDesc)
                .orElse(List.of());
    }

    private void copyFields(Job job, JobForm form) {
        job.setTitle(form.getTitle().trim());
        job.setDescription(form.getDescription().trim());
        job.setLocation(form.getLocation().trim());
        job.setJobType(form.getJobType());
        job.setSalaryMin(form.getSalaryMin());
        job.setSalaryMax(form.getSalaryMax());
    }
}