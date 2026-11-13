package com.example.jobportal.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class HomeController {

    @GetMapping("/")
    public String index() {
        return "index";            // templates/index.html
    }

    // TEMPORARY: shows who is logged in. We turn this into a redirect once the role pages exist.
    @GetMapping("/dashboard")
    public String dashboard() {
        return "dashboard";        // templates/dashboard.html
    }
}