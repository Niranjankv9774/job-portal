package com.example.jobportal.controller;

import com.example.jobportal.dto.CompanyForm;
import com.example.jobportal.dto.JobForm;
import com.example.jobportal.entity.Job;
import com.example.jobportal.entity.User;
import com.example.jobportal.exception.BusinessException;
import com.example.jobportal.service.JobService;
import com.example.jobportal.service.ProfileService;
import com.example.jobportal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** Everything a recruiter can do. SecurityConfig already limits /recruiter/** to recruiters. */
@Controller
@RequestMapping("/recruiter")
@RequiredArgsConstructor
public class RecruiterController {

    private final UserService userService;
    private final ProfileService profileService;
    private final JobService jobService;

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

    // NEW: show the form filled with the job's current values
    @GetMapping("/jobs/{id}/edit")
    public String editJobForm(@PathVariable Long id, Principal principal, Model model) {
        User user = userService.getByEmail(principal.getName());
        Job job = jobService.getOwnedJob(id, user);          // ownership check
        model.addAttribute("jobForm", JobForm.from(job));
        addFormInfo(model, "Edit job", "/recruiter/jobs/" + id + "/edit");
        return "recruiter/job-form";
    }

    // NEW: save the edits
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

    // NEW: close a job
    @PostMapping("/jobs/{id}/close")
    public String closeJob(@PathVariable Long id, Principal principal, RedirectAttributes redirect) {
        jobService.closeJob(id, userService.getByEmail(principal.getName()));
        redirect.addFlashAttribute("success", "Job closed.");
        return "redirect:/recruiter/jobs";
    }

    private void addFormInfo(Model model, String heading, String action) {
        model.addAttribute("heading", heading);
        model.addAttribute("formAction", action);
    }
}