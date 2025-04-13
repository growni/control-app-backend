package com.example.Backend.controllers;

import com.example.Backend.entities.DTO.FeedbackRequest;
import com.example.Backend.entities.DTO.SubscribeRequest;
import com.example.Backend.repositories.SubscriberRepository;
import com.example.Backend.services.Impl.UserServiceImpl;
import com.example.Backend.services.SubscriptionService;
import com.example.Backend.utils.messages;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/profile")
public class UserProfileController {

    private final UserServiceImpl userService;
    private final SubscriptionService subscriptionService;
    private final SubscriberRepository subscriberRepository;

    public UserProfileController(UserServiceImpl userService, SubscriptionService subscriptionService, SubscriberRepository subscriberRepository) {
        this.userService = userService;
        this.subscriptionService = subscriptionService;
        this.subscriberRepository = subscriberRepository;
    }

    @PutMapping("/update-password")
    public ResponseEntity<String> updatePassword(@RequestBody Map<String, String> request) {
        String username = request.get("username");
        String newPassword = request.get("newPassword");

        List<String> validationMessages = userService.updateUserPassword(username, newPassword);
        boolean isPasswordUpdated = validationMessages.size() == 0;

        if(isPasswordUpdated) {
            return ResponseEntity.ok(messages.USER_SETTINGS_SAVED.getMessage());
        } else {
            return ResponseEntity.badRequest().body(String.join("\n", validationMessages));
        }
    }

    @PostMapping("/subscribe")
    public ResponseEntity<String> subscribe(@RequestBody SubscribeRequest request) {
        if(subscriptionService.subscribe(request.getEmail())) {
            return ResponseEntity.ok("You are now subscribed to receive patch notes on your email for each update.");
        } else {
            return ResponseEntity.badRequest().body("You are already subscribed.");
        }
    }

    @PostMapping("/unsubscribe")
    public ResponseEntity<String> unsubscribe(@RequestBody SubscribeRequest request) {
        if(subscriptionService.unsubscribe(request.getEmail())) {
            return ResponseEntity.ok("Successfully unsubscribed.");
        } else {
            return ResponseEntity.badRequest().body("You are not subscribed.");
        }
    }

    @GetMapping("/is-subscribed")
    public ResponseEntity<Boolean> isSubscribed(@RequestParam String email) {
        boolean subscribed = subscriberRepository.findByEmail(email).isPresent();

        return ResponseEntity.ok(subscribed);
    }

    @PostMapping("/feedback")
    public ResponseEntity<String> sendFeedback(@RequestBody FeedbackRequest request) {
        try {
            userService.sendFeedback(request);
            return ResponseEntity.ok("Feedback sent successfully.");
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Failed to send feedback.");
        }
    }
}
