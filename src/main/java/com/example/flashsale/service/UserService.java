package com.example.flashsale.service;

import com.example.flashsale.entity.User;
import com.example.flashsale.exception.ConflictException;
import com.example.flashsale.exception.ResourceNotFoundException;
import com.example.flashsale.repository.UserRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class UserService {

    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Transactional
    public User createUser(String name, String email) {
        userRepository.findByEmail(email).ifPresent(existing -> {
            throw new ConflictException("User already exists with email: " + email);
        });

        return userRepository.save(new User(name, email));
    }

    @Transactional(readOnly = true)
    public User getUser(Long id) {
        return userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public List<User> listUsers() {
        return userRepository.findAll();
    }
}
