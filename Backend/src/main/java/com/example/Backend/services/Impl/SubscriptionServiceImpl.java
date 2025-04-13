package com.example.Backend.services.Impl;

import com.example.Backend.repositories.SubscriberRepository;
import com.example.Backend.services.SubscriptionService;
import com.example.Backend.entities.Subscriber;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class SubscriptionServiceImpl implements SubscriptionService {

    private final SubscriberRepository subscriberRepository;

    public SubscriptionServiceImpl(SubscriberRepository subscriberRepository) {
        this.subscriberRepository = subscriberRepository;
    }

    @Override
    public boolean subscribe(String email) {
        if(subscriberRepository.existsByEmail(email)) {
            return false;
        }

        Subscriber subscriber = new Subscriber();
        subscriber.setEmail(email);

        subscriberRepository.save(subscriber);

        return true;
    }

    @Override
    public boolean unsubscribe(String email) {
        Optional<Subscriber> subscriber = subscriberRepository.findByEmail(email);

        if (subscriber.isPresent()) {
            subscriberRepository.delete(subscriber.get());
            return true;
        }
        return false;
    }
}
