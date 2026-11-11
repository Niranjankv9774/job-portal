package com.example.jobportal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "seeker_profiles")
@Getter
@Setter
@NoArgsConstructor
public class SeekerProfile {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // One user has one profile. This creates a user_id column that links to the users table.
    @OneToOne(optional = false)
    @JoinColumn(name = "user_id", unique = true)
    private User user;

    private String phone;

    @Column(length = 1000)
    private String skills;

    private Integer experienceYears;

    // File name of the uploaded resume (we use this on Day 10)
    private String resumePath;
}