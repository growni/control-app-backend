package com.example.Backend.services.Impl;


import com.example.Backend.entities.DTO.LoginRequest;
import com.example.Backend.entities.DTO.RegisterRequest;
import com.example.Backend.entities.PasswordResetToken;
import com.example.Backend.entities.User;
import com.example.Backend.repositories.PasswordResetTokenRepository;
import com.example.Backend.repositories.UserRepository;
import com.example.Backend.utils.messages;
import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Pattern;


@Service
public class AuthService {

    @Autowired
    private final UserRepository userRepository;
    private final PasswordResetTokenRepository tokenRepository;

    private final PasswordEncoder passwordEncoder;
    private final JavaMailSender mailSender;

    public AuthService(UserRepository userRepository, PasswordResetTokenRepository tokenRepository, PasswordEncoder passwordEncoder, JavaMailSender mailSender) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.mailSender = mailSender;
    }

    public List<String> registerUser(RegisterRequest request) {

        List<String> registrationMessages = validateUserFieldsAndReturnMessage(request);

        if(registrationMessages.size() > 1 || !registrationMessages.getFirst().equals(messages.SUCCESSFUL_REGISTRATION.getMessage())) {
            return registrationMessages;
        }


        User user = new User();
        user.setUsername(request.getUsername());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));

        userRepository.save(user);

        return registrationMessages;
    }

    public boolean initiatePasswordReset(String email) {
        Optional<User> user = userRepository.findByEmail(email);

        if(user.isEmpty()) {
            return false;
        }

        String token = UUID.randomUUID().toString();
        Instant expires = Instant.now().plus(Duration.ofMinutes(15));

        PasswordResetToken resetToken = new PasswordResetToken(user.get(), token, expires);

        tokenRepository.deleteAllTokensForUserExcept(user.get(), token);
        tokenRepository.save(resetToken);

        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setTo(email);
            helper.setSubject("Control App Password Reset");

            String link = "https://control-app-free.netlify.app/reset-password?token=" + token;
            String content = String.format("""
            <html>
              <body>
                <p>Hello, <strong><span style ="color: #FFBF00">%s</span></strong>,</p>
                <p>
                  Please click <a href="%s" target="_blank" style="color: #FFBF00;"><u>this link</u></a> to reset your password.
                </p>
                <p>If you did not initiate the password reset, please ignore this email.</p>
                <p>
                  Best regards,<br/>
                  <strong>Control App team</strong>
                </p>
              </body>
            </html>
            """, user.get().getUsername(), link);

            helper.setText(content, true);

            mailSender.send(message);
            return true;
        } catch (MessagingException e) {
            e.printStackTrace();
            return false;
        }

    }

    public boolean resetPassword(String token, String newPassword) {
        Optional<PasswordResetToken> tokenFromDb = tokenRepository.findByToken(token);

        if(tokenFromDb.isEmpty()) {
            return false;
        }

        PasswordResetToken resetToken = tokenFromDb.get();

//        System.out.println("Is token expired: " + resetToken.isExpired());
//        System.out.println("Token expires at: " + resetToken.getExpirationDate());
//        System.out.println("Now: " + Instant.now());

        if(resetToken.isExpired()) {
            return false;
        }

        User user = resetToken.getUser();

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        tokenRepository.delete(resetToken);

        return true;
    }

    public List<String> validateUserFieldsAndReturnMessage(RegisterRequest request) {
        List<String> validationMessages = new ArrayList<>();

        if(userRepository.existsByEmail(request.getEmail())) {
            validationMessages.add(messages.EMAIL_ALREADY_EXISTS.getMessage());
        }

        if(userRepository.existsByUsername(request.getUsername())) {
            validationMessages.add(messages.USERNAME_ALREADY_EXISTS.getMessage());
        }

        if(request.getUsername().isBlank() || request.getUsername().length() < 3) {
            validationMessages.add(messages.USERNAME_NOT_VALID.getMessage());
        }

        if(request.getPassword().isBlank() || request.getPassword().length() < 6 || request.getPassword().length() > 20 || request.getPassword().matches(".*\\s.*")) {
            validationMessages.add(messages.PASSWORD_VALIDATION_ERROR.getMessage());
        }

        Pattern emailPattern = Pattern.compile("^[\\w.-]+@[\\w.-]+\\.[a-zA-Z]{2,}$");

        if(!emailPattern.matcher(request.getEmail()).matches()) {
            validationMessages.add(messages.EMAIL_NOT_VALID.getMessage());
        }

        if(validationMessages.isEmpty()) {
            validationMessages.add(messages.SUCCESSFUL_REGISTRATION.getMessage());
        }

        return validationMessages;
    }

    public List<String> validateUserFieldsAndReturnMessage(LoginRequest request) {
        List<String> validationMessages = new ArrayList<>();

        Optional<User> user = userRepository.findByUsername(request.getUsername());

        if(user.isEmpty()) {
            validationMessages.add(messages.INCORRECT_USERNAME_OR_PASSWORD.getMessage());
        } else if(!passwordEncoder.matches(request.getPassword(), user.get().getPassword())) {
            validationMessages.add(messages.INCORRECT_USERNAME_OR_PASSWORD.getMessage());
        }

        return validationMessages;
    }

}
