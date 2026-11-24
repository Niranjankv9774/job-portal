package com.example.jobportal.service;

import com.example.jobportal.entity.Job;
import com.example.jobportal.entity.JobApplication;
import com.example.jobportal.entity.JobStatus;
import com.example.jobportal.entity.Role;
import com.example.jobportal.entity.User;
import com.example.jobportal.exception.BusinessException;
import com.example.jobportal.repository.JobApplicationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class ApplicationService {

    private final JobApplicationRepository applicationRepository;
    private final JobService jobService;

    public JobApplication apply(Long jobId, User seeker, String coverNote) {
        Job job = jobService.getById(jobId);

        // The rules, checked in order
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

    // Used by the job details page to hide the Apply button
    public boolean hasApplied(Job job, User seeker) {
        return applicationRepository.existsByJobAndSeeker(job, seeker);
    }

    public List<JobApplication> myApplications(User seeker) {
        return applicationRepository.findBySeekerOrderByAppliedAtDesc(seeker);
    }
}