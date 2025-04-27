package com.example.Backend;

import com.example.Backend.entities.DTO.RegisterRequest;
import com.example.Backend.entities.PasswordResetToken;
import com.example.Backend.entities.User;
import com.example.Backend.repositories.PasswordResetTokenRepository;
import com.example.Backend.repositories.UserRepository;
import com.example.Backend.services.Impl.AuthService;
import jakarta.mail.internet.MimeMessage;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;

import static com.example.Backend.utils.messages.SUCCESSFUL_REGISTRATION;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(org.mockito.junit.jupiter.MockitoExtension.class)
class AuthServiceTest {

	@InjectMocks
	private AuthService authService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PasswordResetTokenRepository tokenRepository;

	@Mock
	private JavaMailSender mailSender;

	@Mock
	private PasswordEncoder passwordEncoder;

	@BeforeEach
	void setup() {
		lenient().when(userRepository.findByEmail(anyString())).thenReturn(Optional.of(new User()));
	}

	@Test
	void registerUser_validUser_savesUserAndReturnsSuccess() {
		RegisterRequest request = new RegisterRequest("newuser", "securePassword123", "newuser@example.com");

		when(userRepository.existsByEmail("newuser@example.com")).thenReturn(false);
		when(userRepository.existsByUsername("newuser")).thenReturn(false);
		when(passwordEncoder.encode(any(CharSequence.class))).thenReturn("encodedPassword");

		List<String> result = authService.registerUser(request);

		assertEquals(1, result.size());
		assertEquals(SUCCESSFUL_REGISTRATION.getMessage(), result.get(0));
		verify(userRepository, times(1)).save(any(User.class));
	}

	@Test
	void registerUser_invalidUser_doesNotSaveAndReturnsErrors() {
		RegisterRequest request = new RegisterRequest("existinguser", "123", "existing@example.com");

		when(userRepository.existsByEmail("existing@example.com")).thenReturn(true);
		when(userRepository.existsByUsername("existinguser")).thenReturn(true);

		List<String> result = authService.registerUser(request);

		assertTrue(result.size() > 1);
		verify(userRepository, never()).save(any(User.class));
	}

	@Test
	void initiatePasswordReset_userExists_returnsTrue() {
		User user = new User();
		user.setEmail("test@example.com");

		when(userRepository.findByEmail("test@example.com")).thenReturn(Optional.of(user));

		MimeMessage mimeMessage = mock(MimeMessage.class);
		when(mailSender.createMimeMessage()).thenReturn(mimeMessage);

		boolean result = authService.initiatePasswordReset("test@example.com");

		assertTrue(result);
		verify(tokenRepository, times(1)).save(any(PasswordResetToken.class));
	}

	@Test
	void initiatePasswordReset_userDoesNotExist_returnsFalse() {
		lenient().when(userRepository.findByEmail("notfound@example.com")).thenReturn(Optional.empty());

		boolean result = authService.initiatePasswordReset("notfound@example.com");

		assertFalse(result);
		verify(tokenRepository, never()).save(any(PasswordResetToken.class));
		verify(mailSender, never()).send(any(MimeMessage.class));
	}
}
