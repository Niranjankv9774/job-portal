package com.example.jobportal.controller;

import com.example.jobportal.dto.SeekerProfileForm;
import com.example.jobportal.entity.SeekerProfile;
import com.example.jobportal.entity.User;
import com.example.jobportal.exception.BusinessException;
import com.example.jobportal.service.ApplicationService;
import com.example.jobportal.service.ProfileService;
import com.example.jobportal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.security.Principal;

/** Pages for job seekers. SecurityConfig limits /seeker/** to the SEEKER role. */
@Controller
@RequestMapping("/seeker")
@RequiredArgsConstructor
public class SeekerController {

    private final UserService userService;
    private final ProfileService profileService;
    private final ApplicationService applicationService;

    @GetMapping("/profile")
    public String profileForm(Principal principal, Model model) {
        User user = userService.getByEmail(principal.getName());
        SeekerProfile profile = profileService.getSeekerProfile(user);
        model.addAttribute("profileForm", SeekerProfileForm.from(profile));
        model.addAttribute("hasResume", profile.getResumePath() != null);
        return "seeker/profile";
    }

    @PostMapping("/profile")
    public String saveProfile(@Valid @ModelAttribute("profileForm") SeekerProfileForm form,
                              BindingResult result,
                              @RequestParam(value = "resume", required = false) MultipartFile resume,
                              Principal principal,
                              Model model,
                              RedirectAttributes redirect) {
        User user = userService.getByEmail(principal.getName());
        if (result.hasErrors()) {
            model.addAttribute("hasResume", profileService.getSeekerProfile(user).getResumePath() != null);
            return "seeker/profile";
        }
        try {
            profileService.saveSeekerProfile(user, form, resume);
        } catch (BusinessException e) {
            // e.g. "Only PDF files are allowed"
            model.addAttribute("error", e.getMessage());
            model.addAttribute("hasResume", profileService.getSeekerProfile(user).getResumePath() != null);
            return "seeker/profile";
        }
        redirect.addFlashAttribute("success", "Profile saved.");
        return "redirect:/seeker/profile";
    }

    @GetMapping("/applications")
    public String myApplications(Principal principal, Model model) {
        User user = userService.getByEmail(principal.getName());
        model.addAttribute("applications", applicationService.myApplications(user));
        return "seeker/applications";
    }
}