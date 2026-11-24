package com.example.jobportal.dto;

import com.example.jobportal.entity.SeekerProfile;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class SeekerProfileForm {

    @Size(max = 20, message = "Phone number is too long")
    private String phone;

    @Size(max = 1000, message = "Skills text is too long")
    private String skills;

    @PositiveOrZero(message = "Experience cannot be negative")
    @Max(value = 60, message = "Enter a realistic number of years")
    private Integer experienceYears;

    // Fills the form from an existing profile
    public static SeekerProfileForm from(SeekerProfile p) {
        SeekerProfileForm f = new SeekerProfileForm();
        f.setPhone(p.getPhone());
        f.setSkills(p.getSkills());
        f.setExperienceYears(p.getExperienceYears());
        return f;
    }
}