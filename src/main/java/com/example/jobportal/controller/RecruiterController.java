package com.example.jobportal.controller;

import com.example.jobportal.dto.CompanyForm;
import com.example.jobportal.dto.JobForm;
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
            // no company yet: send them to create one, with the message
            redirect.addFlashAttribute("error", e.getMessage());
            return "redirect:/recruiter/company";
        }
        redirect.addFlashAttribute("success", "Job posted.");
        return "redirect:/recruiter/jobs";
    }

    // The same job form page is reused for "new" and (Day 8) "edit"
    private void addFormInfo(Model model, String heading, String action) {
        model.addAttribute("heading", heading);
        model.addAttribute("formAction", action);
    }
}