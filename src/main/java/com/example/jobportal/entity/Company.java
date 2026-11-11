package com.example.jobportal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One recruiter has one company profile
    @OneToOne(optional = false)
    @JoinColumn(name = "recruiter_id", unique = true)
    private User recruiter;

    @Column(nullable = false)
    private String name;

    private String website;

    private String location;

    @Column(length = 2000)
    private String description;
}