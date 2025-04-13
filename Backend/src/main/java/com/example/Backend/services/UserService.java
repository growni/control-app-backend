package com.example.Backend.services;

import com.example.Backend.entities.DTO.FeedbackRequest;
import com.example.Backend.entities.DTO.UserDTO;
import com.example.Backend.entities.User;

import java.util.List;

public interface UserService {
    User registerUser(UserDTO userDTO);
    User getUserByEmail(String email);
    String getEmailByUsername(String username);
    List<String> updateUserPassword(String username, String newPassword);
    void sendFeedback(FeedbackRequest feedbackRequest);
}
