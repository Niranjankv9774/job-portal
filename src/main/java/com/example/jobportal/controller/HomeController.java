package com.example.jobportal.controller;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    // After login: send each role to its own page.
    // Admins still get the temporary welcome page until Day 11.
    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication) {
        List<String> roles = authentication.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .toList();
        if (roles.contains("ROLE_RECRUITER")) {
            return "redirect:/recruiter/jobs";
        }
        if (roles.contains("ROLE_SEEKER")) {
            return "redirect:/seeker/applications";
        }
        return "dashboard";
    }
}