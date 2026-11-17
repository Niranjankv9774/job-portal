package com.example.jobportal.controller;

import com.example.jobportal.service.JobService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

/** Public job list and details. Anyone can open these pages. */
@Controller
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;

    @GetMapping("/jobs")
    public String list(@RequestParam(defaultValue = "") String keyword,
                       @RequestParam(defaultValue = "") String location,
                       @RequestParam(defaultValue = "0") int page,
                       Model model) {
        model.addAttribute("jobs", jobService.search(keyword, location, page));
        model.addAttribute("keyword", keyword);      // sent back so the search boxes stay filled
        model.addAttribute("location", location);
        return "jobs";
    }

    @GetMapping("/jobs/{id}")
    public String details(@PathVariable Long id, Model model) {
        model.addAttribute("job", jobService.getById(id));
        return "job-details";
    }
}