package com.example.Backend.controllers;

import com.example.Backend.entities.DTO.LoginRequest;
import com.example.Backend.entities.DTO.RegisterRequest;
import com.example.Backend.entities.DTO.ResetPasswordRequest;
import com.example.Backend.services.Impl.AuthService;
import com.example.Backend.services.Impl.UserServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.example.Backend.utils.messages.SUCCESSFUL_REGISTRATION;

@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    private final UserServiceImpl userService;

    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private BCryptPasswordEncoder bCryptPasswordEncoder;

    public AuthController(AuthService authService, UserServiceImpl userServiceImpl) {
        this.authService = authService;
        this.userService = userServiceImpl;
    }

    @PostMapping(value = "/register", consumes = "application/json", produces = "text/plain")
    public ResponseEntity<String> register(@RequestBody RegisterRequest request) {
        List<String> registrationMessages = authService.registerUser(request);

        if(registrationMessages.getFirst().equals(SUCCESSFUL_REGISTRATION.getMessage())) {
            return ResponseEntity.ok(SUCCESSFUL_REGISTRATION.getMessage());
        } else {
            return ResponseEntity.badRequest().body(String.join("\n", registrationMessages));
        }
    }

    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        List<String> loginMessages = authService.validateUserFieldsAndReturnMessage(request);

        try {
            authenticationManager.authenticate(new UsernamePasswordAuthenticationToken(request.getUsername(), request.getPassword()));

            Map<String, String> response = new HashMap<>();
            response.put("username", request.getUsername());
            response.put("email", userService.getEmailByUsername(request.getUsername()));

            return ResponseEntity.ok(response);
        } catch(BadCredentialsException ex) {
            return ResponseEntity.badRequest().body(String.join("\n", loginMessages));
        }
    }

    @PostMapping("/send-password-reset-link")
    public ResponseEntity<String> sendPasswordResetLink(@RequestBody Map<String, String> request) {
        String email = request.get("email");
        boolean resetLinkSent = authService.initiatePasswordReset(email);

        if(resetLinkSent) {
            return ResponseEntity.ok("Reset link sent to your email.");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found.");
        }
    }

    @PostMapping("/reset-password")
    public ResponseEntity<?> passwordReset(@RequestBody ResetPasswordRequest request) {

        boolean isResetSuccessful = authService.resetPassword(request.getToken(), request.getPassword());

        if(isResetSuccessful) {
            return ResponseEntity.ok("Password was successfully reset!");
        } else {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Invalid or expired token.");
        }
    }
}
