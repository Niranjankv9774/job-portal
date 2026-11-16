package com.example.jobportal.service;

import com.example.jobportal.dto.JobForm;
import com.example.jobportal.entity.Company;
import com.example.jobportal.entity.Job;
import com.example.jobportal.entity.User;
import com.example.jobportal.exception.BusinessException;
import com.example.jobportal.repository.CompanyRepository;
import com.example.jobportal.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final CompanyRepository companyRepository;

    public Job createJob(User recruiter, JobForm form) {
        // A job needs a company, so the recruiter must create one first
        Company company = companyRepository.findByRecruiter(recruiter)
                .orElseThrow(() -> new BusinessException(
                        "Please create your company profile before posting jobs"));
        Job job = new Job();
        job.setCompany(company);
        copyFields(job, form);
        return jobRepository.save(job);
    }

    // All jobs of this recruiter's company, newest first (empty list if no company yet)
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