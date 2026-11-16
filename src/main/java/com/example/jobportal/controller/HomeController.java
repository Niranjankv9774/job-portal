package com.example.jobportal.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    // After login: recruiters go to their jobs page. Seekers and admins get the
    // temporary welcome page until we build their pages (Days 9 and 11).
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        boolean recruiter = authentication.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equals("ROLE_RECRUITER"));
        if (recruiter) {
            return "redirect:/recruiter/jobs";
        }
        return "dashboard";
    }
}