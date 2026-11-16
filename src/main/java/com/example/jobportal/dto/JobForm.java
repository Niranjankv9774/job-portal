package com.example.jobportal.dto;

import com.example.jobportal.entity.Job;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class JobForm {

    @NotBlank(message = "Title is required")
    @Size(max = 150, message = "Title is too long")
    private String title;

    @NotBlank(message = "Description is required")
    @Size(max = 4000, message = "Description is too long")
    private String description;

    @NotBlank(message = "Location is required")
    private String location;

    @NotBlank(message = "Job type is required")
    private String jobType;

    @PositiveOrZero(message = "Salary cannot be negative")
    private Integer salaryMin;

    @PositiveOrZero(message = "Salary cannot be negative")
    private Integer salaryMax;

    // Fills the form from an existing job (we use this on Day 8 for editing)
    public static JobForm from(Job job) {
        JobForm f = new JobForm();
        f.setTitle(job.getTitle());
        f.setDescription(job.getDescription());
        f.setLocation(job.getLocation());
        f.setJobType(job.getJobType());
        f.setSalaryMin(job.getSalaryMin());
        f.setSalaryMax(job.getSalaryMax());
        return f;
    }
}