package com.example.demo;

import org.springframework.stereotype.Service;

@Service
public class HelloService {

    public String greet(String name) {
        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (name.length() > 50) {
            throw new IllegalArgumentException("Name is too long");
        }
        return "Hello, " + name + "!";
    }

    public int calculateAge(int birthYear, int currentYear) {
        if (birthYear > currentYear) {
            throw new IllegalArgumentException("Birth year cannot be in the future");
        }
        return currentYear - birthYear;
    }
}
