package com.example.Backend.entities.DTO;

import lombok.Builder;
import lombok.Data;
import lombok.Getter;

@Data
public class UserDTO {
    private String username;
    private String password;
    private String email;


    public String getUsername() {
        return username;
    }

    public String getPassword() {
        return password;
    }

    public String getEmail() {
        return email;
    }
}
