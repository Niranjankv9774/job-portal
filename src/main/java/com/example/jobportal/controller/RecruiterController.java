package com.example.jobportal.controller;

import com.example.jobportal.dto.CompanyForm;
import com.example.jobportal.dto.JobForm;
import com.example.jobportal.entity.ApplicationStatus;
import com.example.jobportal.entity.Job;
import com.example.jobportal.entity.JobApplication;
import com.example.jobportal.entity.User;
import com.example.jobportal.exception.BusinessException;
import com.example.jobportal.exception.ResourceNotFoundException;
import com.example.jobportal.service.ApplicationService;
import com.example.jobportal.service.FileStorageService;
import com.example.jobportal.service.JobService;
import com.example.jobportal.service.ProfileService;
import com.example.jobportal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/** Everything a recruiter can do. SecurityConfig already limits /recruiter/** to recruiters. */
@Controller
@RequestMapping("/recruiter")
@RequiredArgsConstructor
public class RecruiterController {

    private final UserService userService;
    private final ProfileService profileService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final FileStorageService fileStorageService;

    // ---------- Company profile ----------

    @GetMapping("/company")
    public String companyForm(Principal principal, Model model) {
        User user = userService.getByEmail(principal.getName());
        model.addAttribute("companyForm",
                profileService.getCompany(user).map(CompanyForm::from).orElseGet(CompanyForm::new));
        return "recruiter/company";
    }

    @PostMapping("/company")
    public String saveCompany(@Valid @ModelAttribute("companyForm") CompanyForm form,
                              BindingResult result,
                              Principal principal,
                              RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "recruiter/company";
        }
        profileService.saveCompany(userService.getByEmail(principal.getName()), form);
        redirect.addFlashAttribute("success", "Company profile saved.");
        return "redirect:/recruiter/company";
    }

    // ---------- Jobs ----------

    @GetMapping("/jobs")
    public String myJobs(Principal principal, Model model) {
        User user = userService.getByEmail(principal.getName());
        model.addAttribute("jobs", jobService.jobsOf(user));
        return "recruiter/jobs";
    }

    @GetMapping("/jobs/new")
    public String newJobForm(Model model) {
        model.addAttribute("jobForm", new JobForm());
        addFormInfo(model, "Post a job", "/recruiter/jobs/new");
        return "recruiter/job-form";
    }

    @PostMapping("/jobs/new")
    public String createJob(@Valid @ModelAttribute("jobForm") JobForm form,
                            BindingResult result,
                            Principal principal,
                            Model model,
                            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            addFormInfo(model, "Post a job", "/recruiter/jobs/new");
            return "recruiter/job-form";
        }
        try {
            jobService.createJob(userService.getByEmail(principal.getName()), form);
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/recruiter/company";
        }
        redirect.addFlashAttribute("success", "Job posted.");
        return "redirect:/recruiter/jobs";
    }

    @GetMapping("/jobs/{id}/edit")
    public String editJobForm(@PathVariable Long id, Principal principal, Model model) {
        User user = userService.getByEmail(principal.getName());
        Job job = jobService.getOwnedJob(id, user);
        model.addAttribute("jobForm", JobForm.from(job));
        addFormInfo(model, "Edit job", "/recruiter/jobs/" + id + "/edit");
        return "recruiter/job-form";
    }

    @PostMapping("/jobs/{id}/edit")
    public String updateJob(@PathVariable Long id,
                            @Valid @ModelAttribute("jobForm") JobForm form,
                            BindingResult result,
                            Principal principal,
                            Model model,
                            RedirectAttributes redirect) {
        if (result.hasErrors()) {
            addFormInfo(model, "Edit job", "/recruiter/jobs/" + id + "/edit");
            return "recruiter/job-form";
        }
        jobService.updateJob(id, userService.getByEmail(principal.getName()), form);
        redirect.addFlashAttribute("success", "Job updated.");
        return "redirect:/recruiter/jobs";
    }

    @PostMapping("/jobs/{id}/close")
    public String closeJob(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        jobService.closeJob(id, userService.getByEmail(principal.getName()));
        redirect.addFlashAttribute("success", "Job closed.");
        return "redirect:/recruiter/jobs";
    }

    // ---------- Applicants (NEW today) ----------

    @GetMapping("/jobs/{id}/applicants")
    public String applicants(@PathVariable Long id, Principal principal, Model model) {
        User user = userService.getByEmail(principal.getName());
        Job job = jobService.getOwnedJob(id, user);                           // ownership check
        List<JobApplication> applications = applicationService.applicantsFor(id, user);

        // Which applicants actually uploaded a resume? (so we only show Download for them)
        Set<Long> withResume = applications.stream()
                .filter(a -> profileService.getSeekerProfile(a.getSeeker()).getResumePath() != null)
                .map(JobApplication::getId)
                .collect(Collectors.toSet());

        model.addAttribute("job", job);
        model.addAttribute("applications", applications);
        model.addAttribute("withResume", withResume);
        model.addAttribute("statuses", ApplicationStatus.values());
        return "recruiter/applicants";
    }

    @PostMapping("/applications/{id}/status")
    public String updateStatus(@PathVariable Long id,
                               @RequestParam ApplicationStatus status,
                               Principal principal,
                               RedirectAttributes redirect) {
        JobApplication application = applicationService.updateStatus(
                id, userService.getByEmail(principal.getName()), status);
        redirect.addFlashAttribute("success", "Status updated.");
        return "redirect:/recruiter/jobs/" + application.getJob().getId() + "/applicants";
    }

    @GetMapping("/applications/{id}/resume")
    public ResponseEntity<Resource> downloadResume(@PathVariable Long id, Principal principal) {
        // Ownership check: only the recruiter who owns this job may download
        JobApplication application = applicationService.getOwnedApplication(
                id, userService.getByEmail(principal.getName()));

        String stored = profileService.getSeekerProfile(application.getSeeker()).getResumePath();
        if (stored == null) {
            throw new ResourceNotFoundException("This applicant has not uploaded a resume");
        }
        String safeName = application.getSeeker().getName().replaceAll("[^a-zA-Z0-9]", "_");
        return ResponseEntity.ok()
                .contentType(MediaType.APPLICATION_PDF)
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=\"resume-" + safeName + ".pdf\"")
                .body(fileStorageService.load(stored));
    }

    private void addFormInfo(Model model, String heading, String action) {
        model.addAttribute("heading", heading);
        model.addAttribute("formAction", action);
    }
}