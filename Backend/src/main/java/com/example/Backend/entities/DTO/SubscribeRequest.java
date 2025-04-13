package com.example.Backend.entities.DTO;

public class SubscribeRequest {
    private String email;

    public SubscribeRequest(String email) {
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
