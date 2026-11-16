package com.example.jobportal.service;

import com.example.jobportal.dto.CompanyForm;
import com.example.jobportal.entity.Company;
import com.example.jobportal.entity.User;
import com.example.jobportal.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final CompanyRepository companyRepository;

    public Optional<Company> getCompany(User recruiter) {
        return companyRepository.findByRecruiter(recruiter);
    }

    // Creates the company the first time, updates it after that
    @Transactional
    public Company saveCompany(User recruiter, CompanyForm form) {
        Company company = companyRepository.findByRecruiter(recruiter).orElseGet(() -> {
            Company c = new Company();
            c.setRecruiter(recruiter);
            return c;
        });
        company.setName(form.getName().trim());
        company.setWebsite(form.getWebsite());
        company.setLocation(form.getLocation());
        company.setDescription(form.getDescription());
        return companyRepository.save(company);
    }
}