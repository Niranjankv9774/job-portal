package com.example.jobportal.controller;

import com.example.jobportal.entity.User;
import com.example.jobportal.service.ApplicationService;
import com.example.jobportal.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.security.Principal;

/** Pages for job seekers. SecurityConfig limits /seeker/** to the SEEKER role. */
@Controller
@RequestMapping("/seeker")
@RequiredArgsConstructor
public class SeekerController {

    private final UserService userService;
    private final ApplicationService applicationService;

    @GetMapping("/applications")
    public String myApplications(Principal principal, Model model) {
        User user = userService.getByEmail(principal.getName());
        model.addAttribute("applications", applicationService.myApplications(user));
        return "seeker/applications";
    }
}