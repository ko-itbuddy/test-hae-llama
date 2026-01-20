package com.example.demo.service;

import com.example.demo.event.UserCreatedEvent;
import com.example.demo.model.User;
import com.example.demo.repository.UserRepository;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;

import java.util.List;

import org.springframework.transaction.annotation.Transactional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final ApplicationEventPublisher eventPublisher;

    public UserService(UserRepository userRepository, ApplicationEventPublisher eventPublisher) {
        this.userRepository = userRepository;
        this.eventPublisher = eventPublisher;
    }

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    @Transactional
    public User createUser(String name, String email) {
        if (name == null) {
            throw new IllegalArgumentException("name cannot be null");
        }
        if (name.trim().isEmpty()) {
            throw new IllegalArgumentException("name cannot be empty");
        }
        if (email == null) {
            throw new IllegalArgumentException("Email cannot be null");
        }
        if (email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email already exists");
        }
        User user = new User();
        user.setName(name);
        user.setEmail(email);
        User savedUser = userRepository.save(user);

        eventPublisher.publishEvent(new UserCreatedEvent(this, savedUser));

        return savedUser;
    }
}
