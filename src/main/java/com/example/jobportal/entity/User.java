package com.example.jobportal.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

@Entity                          // this class becomes a table
@Table(name = "users")           // table name (user is a reserved word in some databases)
@Getter                          // Lombok writes all getters
@Setter                          // Lombok writes all setters
@NoArgsConstructor               // Lombok writes an empty constructor (JPA needs one)
public class User {

    @Id                                                  // primary key
    @GeneratedValue(strategy = GenerationType.IDENTITY)  // MySQL auto-increments it
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)             // no two users with the same email
    private String email;

    @Column(nullable = false)
    private String password;                             // will hold a BCrypt hash, never plain text

    @Enumerated(EnumType.STRING)                         // store "SEEKER", not the number 0
    @Column(nullable = false)
    private Role role;

    private boolean active = true;                       // admin can block users later

    private LocalDateTime createdAt = LocalDateTime.now();
}