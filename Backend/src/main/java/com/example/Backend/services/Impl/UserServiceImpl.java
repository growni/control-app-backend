package com.example.Backend.services.Impl;

import com.example.Backend.entities.DTO.FeedbackRequest;
import com.example.Backend.entities.DTO.UserDTO;
import com.example.Backend.entities.User;
import com.example.Backend.repositories.UserRepository;
import com.example.Backend.services.UserService;
import com.example.Backend.utils.messages;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepository;
    private final BCryptPasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public UserServiceImpl(UserRepository userRepository, BCryptPasswordEncoder passwordEncoder, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    @Override
    public User registerUser(UserDTO userDTO) {

        User user = new User(userDTO.getUsername(), userDTO.getPassword(), userDTO.getEmail());

        userRepository.save(user);
        System.out.println(messages.SUCCESSFUL_REGISTRATION.getMessage());

        return user;
    }

    @Override
    public User getUserByEmail(String email) {
        return userRepository.findByEmail(email).orElseThrow(() -> new RuntimeException(String.format(messages.USER_NOT_FOUND.getMessage(), email)));
    }

    @Override
    public String getEmailByUsername(String username) {
        return userRepository.getEmailByUsername(username);
    }

    @Override
    public List<String> updateUserPassword(String username, String newPassword) {
        ArrayList<String> validationMessages = new ArrayList<>();

        if(newPassword.isBlank() || newPassword.length() < 6 || newPassword.length() > 20 || newPassword.matches(".*\\s.*")) {
            validationMessages.add(messages.PASSWORD_VALIDATION_ERROR.getMessage());
        }

        if(!userRepository.existsByUsername(username)) {
            validationMessages.add(messages.USER_NOT_FOUND.getMessage());
        }

        String encodedPassword = passwordEncoder.encode(newPassword);
        int updated = userRepository.updateUserPassword(username, encodedPassword);

        return validationMessages;
    }

    @Override
    public void sendFeedback(FeedbackRequest request) {
        if (request.getEmail() == null || request.getMessage() == null || request.getMessage().isBlank()) {
            throw new IllegalArgumentException("Email and message cannot be empty.");
        }

        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("control.desktop.app@gmail.com");
        message.setSubject("User feedback from: " + request.getEmail());
        message.setText(request.getMessage());

        mailSender.send(message);
    }
}
