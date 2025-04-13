package com.example.Backend.entities.DTO;

public class FeedbackRequest {
    private String email;
    private String message;

    public FeedbackRequest() {
    }

    public FeedbackRequest(String message, String email) {
        this.message = message;
        this.email = email;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}
