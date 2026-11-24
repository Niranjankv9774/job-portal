package com.example.jobportal.service;

import com.example.jobportal.dto.CompanyForm;
import com.example.jobportal.dto.SeekerProfileForm;
import com.example.jobportal.entity.Company;
import com.example.jobportal.entity.SeekerProfile;
import com.example.jobportal.entity.User;
import com.example.jobportal.repository.CompanyRepository;
import com.example.jobportal.repository.SeekerProfileRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class ProfileService {

    private final SeekerProfileRepository seekerProfileRepository;
    private final CompanyRepository companyRepository;
    private final FileStorageService fileStorageService;

    /** Returns the saved profile, or a new empty (unsaved) one for first-time users. */
    public SeekerProfile getSeekerProfile(User user) {
        return seekerProfileRepository.findByUser(user).orElseGet(() -> {
            SeekerProfile profile = new SeekerProfile();
            profile.setUser(user);
            return profile;
        });
    }

    @Transactional
    public SeekerProfile saveSeekerProfile(User user, SeekerProfileForm form, MultipartFile resume) {
        SeekerProfile profile = getSeekerProfile(user);
        profile.setPhone(form.getPhone());
        profile.setSkills(form.getSkills());
        profile.setExperienceYears(form.getExperienceYears());

        // Only touch the resume if the user chose a new file
        if (resume != null && !resume.isEmpty()) {
            String storedName = fileStorageService.storeResume(resume);   // throws if not a valid PDF
            String oldName = profile.getResumePath();
            profile.setResumePath(storedName);
            if (oldName != null) {
                fileStorageService.delete(oldName);                       // remove the previous file
            }
        }
        return seekerProfileRepository.save(profile);
    }

    public Optional<Company> getCompany(User recruiter) {
        return companyRepository.findByRecruiter(recruiter);
    }

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