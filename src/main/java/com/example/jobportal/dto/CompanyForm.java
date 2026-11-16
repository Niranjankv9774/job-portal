package com.example.jobportal.dto;

import com.example.jobportal.entity.Company;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CompanyForm {

    @NotBlank(message = "Company name is required")
    @Size(max = 150, message = "Name is too long")
    private String name;

    private String website;

    private String location;

    @Size(max = 2000, message = "Description is too long")
    private String description;

    // Fills the form from an existing company (used when editing)
    public static CompanyForm from(Company c) {
        CompanyForm f = new CompanyForm();
        f.setName(c.getName());
        f.setWebsite(c.getWebsite());
        f.setLocation(c.getLocation());
        f.setDescription(c.getDescription());
        return f;
    }
}