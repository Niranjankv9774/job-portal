package com.example.jobportal.controller;

import com.example.jobportal.entity.Job;
import com.example.jobportal.entity.Role;
import com.example.jobportal.entity.User;
import com.example.jobportal.exception.BusinessException;
import com.example.jobportal.service.ApplicationService;
import com.example.jobportal.service.JobService;
import com.example.jobportal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** Public job list and details, plus the "apply" action for seekers. */
@Controller
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final ApplicationService applicationService;
    private final UserService userService;

    @GetMapping("/jobs")
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "") String location,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        model.addAttribute("jobs", jobService.search(keyword, location, page));
        model.addAttribute("keyword", keyword);
        model.addAttribute("location", location);
        return "jobs";
    }

    @GetMapping("/jobs/{id}")
    public String details(@PathVariable Long id, Principal principal, Model model) {
        Job job = jobService.getById(id);

        // principal is null when nobody is logged in
        boolean applied = false;
        if (principal != null) {
            User user = userService.getByEmail(principal.getName());
            if (user.getRole() == Role.SEEKER) {
                applied = applicationService.hasApplied(job, user);
            }
        }
        model.addAttribute("job", job);
        model.addAttribute("applied", applied);
        return "job-details";
    }

    // NEW: only seekers get here (SecurityConfig: POST /jobs/{id}/apply needs ROLE_SEEKER)
    @PostMapping("/jobs/{id}/apply")
    public String apply(@PathVariable Long id,
                        @RequestParam(required = false) String coverNote,
                        Principal principal,
                        RedirectAttributes redirect) {
        User seeker = userService.getByEmail(principal.getName());
        try {
            applicationService.apply(id, seeker, coverNote);
            redirect.addFlashAttribute("success", "Application submitted. Good luck!");
        } catch (BusinessException e) {
            redirect.addFlashAttribute("error", e.getMessage());
        }
        return "redirect:/jobs/" + id;
    }
}