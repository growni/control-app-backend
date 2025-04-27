package com.example.Backend;

import com.example.Backend.entities.DTO.FeedbackRequest;
import com.example.Backend.entities.DTO.UserDTO;
import com.example.Backend.entities.User;
import com.example.Backend.repositories.UserRepository;
import com.example.Backend.services.Impl.UserServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

import java.util.Optional;
import java.util.List;

import static com.example.Backend.utils.messages.USER_NOT_FOUND;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private BCryptPasswordEncoder passwordEncoder;

    @Mock
    private JavaMailSender mailSender;

    @InjectMocks
    private UserServiceImpl userService;

    @Test
    void registerUser_validUser_savesUser() {
        UserDTO userDTO = new UserDTO("testuser", "password", "test@example.com");

        User savedUser = userService.registerUser(userDTO);

        assertEquals(userDTO.getUsername(), savedUser.getUsername());
        assertEquals(userDTO.getEmail(), savedUser.getEmail());
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void getUserByEmail_userExists_returnsUser() {
        User user = new User("testuser", "password", "test@example.com");
        when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

        User foundUser = userService.getUserByEmail("test@example.com");

        assertEquals("testuser", foundUser.getUsername());
    }

    @Test
    void getUserByEmail_userDoesNotExist_throwsException() {
        when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class,
                () -> userService.getUserByEmail("notfound@example.com"));

        assertTrue(exception.getMessage().contains(String.format(USER_NOT_FOUND.getMessage(), "notfound@example.com")));
    }

    @Test
    void getEmailByUsername_returnsEmail() {
        when(userRepository.getEmailByUsername("testuser")).thenReturn("test@example.com");

        String email = userService.getEmailByUsername("testuser");

        assertEquals("test@example.com", email);
    }

    @Test
    void updateUserPassword_validRequest_updatesPassword() {
        String username = "testuser";
        String newPassword = "newPassword123";

        when(userRepository.existsByUsername(username)).thenReturn(true);
        when(passwordEncoder.encode(newPassword)).thenReturn("encodedPassword");
        when(userRepository.updateUserPassword(username, "encodedPassword")).thenReturn(1);

        List<String> validationMessages = userService.updateUserPassword(username, newPassword);

        assertTrue(validationMessages.isEmpty());
    }

    @Test
    void updateUserPassword_invalidPassword_returnsValidationError() {
        String username = "testuser";
        String newPassword = "123"; // too short

        when(userRepository.existsByUsername(username)).thenReturn(true);

        List<String> validationMessages = userService.updateUserPassword(username, newPassword);

        assertFalse(validationMessages.isEmpty());
        assertTrue(validationMessages.stream().anyMatch(msg -> msg.contains("Password")));
    }

    @Test
    void sendFeedback_validRequest_sendsEmail() {
        FeedbackRequest request = new FeedbackRequest();
        request.setEmail("user@example.com");
        request.setMessage("This app is awesome!");

        userService.sendFeedback(request);

        verify(mailSender, times(1)).send(any(SimpleMailMessage.class));
    }

    @Test
    void sendFeedback_invalidRequest_throwsException() {
        FeedbackRequest request = new FeedbackRequest();
        request.setEmail(null);
        request.setMessage("");

        assertThrows(IllegalArgumentException.class, () -> userService.sendFeedback(request));
    }
}
