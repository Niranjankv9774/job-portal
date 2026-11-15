package com.example.jobportal.controller;

import com.example.jobportal.dto.ResetPasswordForm;
import com.example.jobportal.exception.BusinessException;
import com.example.jobportal.service.PasswordResetService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequiredArgsConstructor
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    // Page 1: ask for the email
    @GetMapping("/forgot-password")
    public String forgotForm() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String forgotSubmit(@RequestParam String email, RedirectAttributes redirect) {
        passwordResetService.createAndSend(email);
        // Same message whether or not the email exists.
        redirect.addFlashAttribute("success",
                "If this email is registered, a password reset link has been sent.");
        return "redirect:/forgot-password";
    }

    // Page 2: the link from the email opens this
    @GetMapping("/reset-password")
    public String resetForm(@RequestParam(required = false) String token, Model model) {
        if (!passwordResetService.isValid(token)) {
            model.addAttribute("invalid", true);
            return "reset-password";
        }
        ResetPasswordForm form = new ResetPasswordForm();
        form.setToken(token);
        model.addAttribute("form", form);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetSubmit(@Valid @ModelAttribute("form") ResetPasswordForm form,
                              BindingResult result,
                              Model model,
                              RedirectAttributes redirect) {
        if (form.getPassword() != null && !form.getPassword().equals(form.getConfirmPassword())) {
            result.rejectValue("confirmPassword", "mismatch", "Passwords do not match");
        }
        if (result.hasErrors()) {
            return "reset-password";
        }
        try {
            passwordResetService.resetPassword(form.getToken(), form.getPassword());
        } catch (BusinessException e) {
            model.addAttribute("invalid", true);
            return "reset-password";
        }
        redirect.addFlashAttribute("success", "Password changed. You can log in now.");
        return "redirect:/login";
    }
}