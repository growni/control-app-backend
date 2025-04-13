package com.example.Backend.services;

public interface SubscriptionService {
    boolean subscribe(String email);
    boolean unsubscribe(String email);
}
