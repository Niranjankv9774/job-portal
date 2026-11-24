package com.example.jobportal.service;

import com.example.jobportal.entity.ApplicationStatus;
import com.example.jobportal.entity.Job;
import com.example.jobportal.entity.JobApplication;
import com.example.jobportal.entity.JobStatus;
import com.example.jobportal.entity.Role;
import com.example.jobportal.entity.User;
import com.example.jobportal.exception.BusinessException;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobService jobService;

    public JobApplication apply(Long jobId, User seeker, String coverNote) {
        Job job = jobService.getById(jobId);
        if (seeker.getRole() != Role.SEEKER) {
            throw new BusinessException("Only job seekers can apply");
        }
        if (job.getStatus() != JobStatus.OPEN) {
            throw new BusinessException("This job is closed");
        }
        if (applicationRepository.existsByJobAndSeeker(job, seeker)) {
            throw new BusinessException("You have already applied to this job");
        }
        JobApplication application = new JobApplication();
        application.setJob(job);
        application.setSeeker(seeker);
        application.setCoverNote(coverNote == null || coverNote.isBlank() ? null : coverNote.trim());
        return applicationRepository.save(application);
    }

    public boolean hasApplied(Job job, User seeker) {
        return applicationRepository.existsByJobAndSeeker(job, seeker);
    }

    public List<JobApplication> myApplications(User seeker) {
        return applicationRepository.findBySeekerOrderByAppliedAtDesc(seeker);
    }

    // NEW: applicants of a job (the job must belong to this recruiter)
    public List<JobApplication> applicantsFor(Long jobId, User recruiter) {
        Job job = jobService.getOwnedJob(jobId, recruiter);
        return applicationRepository.findByJobOrderByAppliedAtDesc(job);
    }

    // NEW: ownership check for one application: application -> job -> company -> recruiter
    public JobApplication getOwnedApplication(Long applicationId, User recruiter) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new ResourceNotFoundException("Application not found"));
        if (!application.getJob().getCompany().getRecruiter().getId().equals(recruiter.getId())) {
            throw new AccessDeniedException("This application belongs to another recruiter");
        }
        return application;
    }

    // NEW: shortlist, reject or hire
    public JobApplication updateStatus(Long applicationId, User recruiter, ApplicationStatus status) {
        if (status == null) {
            throw new BusinessException("Status is required");
        }
        JobApplication application = getOwnedApplication(applicationId, recruiter);
        application.setStatus(status);
        return applicationRepository.save(application);
    }
}