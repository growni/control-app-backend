package com.example.Backend.entities.DTO;

import lombok.Data;
import lombok.Getter;

@Getter
public class AuthResponse {
    private String token;

    public AuthResponse(String token) {
        this.token = token;
    }
}
