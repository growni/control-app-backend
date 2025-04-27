package com.example.Backend;

import com.example.Backend.entities.Subscriber;
import com.example.Backend.repositories.SubscriberRepository;
import com.example.Backend.services.Impl.SubscriptionServiceImpl;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriberRepository subscriberRepository;

    @InjectMocks
    private SubscriptionServiceImpl subscriptionService;

    @Test
    void subscribe_newEmail_returnsTrueAndSaves() {
        String email = "new@example.com";

        when(subscriberRepository.existsByEmail(email)).thenReturn(false);

        boolean result = subscriptionService.subscribe(email);

        assertTrue(result);
        verify(subscriberRepository, times(1)).save(any(Subscriber.class));
    }

    @Test
    void subscribe_existingEmail_returnsFalse() {
        String email = "existing@example.com";

        when(subscriberRepository.existsByEmail(email)).thenReturn(true);

        boolean result = subscriptionService.subscribe(email);

        assertFalse(result);
        verify(subscriberRepository, never()).save(any(Subscriber.class));
    }

    @Test
    void unsubscribe_existingEmail_returnsTrueAndDeletes() {
        String email = "subscriber@example.com";
        Subscriber subscriber = new Subscriber();
        subscriber.setEmail(email);

        when(subscriberRepository.findByEmail(email)).thenReturn(Optional.of(subscriber));

        boolean result = subscriptionService.unsubscribe(email);

        assertTrue(result);
        verify(subscriberRepository, times(1)).delete(subscriber);
    }

    @Test
    void unsubscribe_nonExistingEmail_returnsFalse() {
        String email = "notfound@example.com";

        when(subscriberRepository.findByEmail(email)).thenReturn(Optional.empty());

        boolean result = subscriptionService.unsubscribe(email);

        assertFalse(result);
        verify(subscriberRepository, never()).delete(any(Subscriber.class));
    }
}
