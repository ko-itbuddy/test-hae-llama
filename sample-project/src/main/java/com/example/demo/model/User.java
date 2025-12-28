package com.example.demo.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.*;
import lombok.*;

@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    @GeneratedValue
    private Long id;

    @NotBlank
    private String name;

    @Email
    @NotBlank
    private String email;
    private String grade;
    private String phoneNumber;

    public String getGrade() { return grade; }
    public String getPhoneNumber() { return phoneNumber; }
    public String getEmail() { return email; }
}