package com.example.jobportal.controller;

import com.example.jobportal.dto.RegisterDto;
import com.example.jobportal.exception.BusinessException;
import com.example.jobportal.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class AuthController {

    private final UserService userService;

    // Show the empty form
    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("registerDto", new RegisterDto());
        return "register";                       // opens templates/register.html
    }

    // Handle the submitted form
    @PostMapping("/register")
    public String register(@Valid @ModelAttribute("registerDto") RegisterDto dto,
                           BindingResult result,           // must come right after the @Valid object
                           Model model,
                           RedirectAttributes redirect) {
        if (result.hasErrors()) {
            return "register";                   // show the form again with error messages
        }
        try {
            userService.register(dto);
        } catch (BusinessException e) {
            model.addAttribute("error", e.getMessage());
            return "register";
        }
        redirect.addFlashAttribute("success", "Registration successful! Login will be added tomorrow.");
        return "redirect:/register";             // Day 4 changes this to /login
    }
}