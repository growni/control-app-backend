package com.example.Backend.utils;

import lombok.Getter;


public enum messages {
    EMAIL_ALREADY_EXISTS("Email already in use."),
    USERNAME_ALREADY_EXISTS("Username is already in use."),
    EMAIL_NOT_VALID("Provided email is not valid."),
    SUCCESSFUL_REGISTRATION("Registration completed."),
    USER_NOT_FOUND("User with email %s not found."),
    USERNAME_OR_EMAIL_EXISTS("Username or email already in use"),
    USERNAME_NOT_VALID("Provided username is not valid."),
    PASSWORD_NOT_VALID("Provided password is not valid."),
    INCORRECT_USERNAME_OR_PASSWORD("Provided username or password is not valid."),
    NEW_PASSWORD_SAME_AS_OLD_PASSWORD("Your new password can't be your old password."),
    USER_SETTINGS_SAVED("Your changes have been saved!"),
    FAILED_TO_UPDATE_PASSWORD("Failed to update password."),
    PASSWORD_VALIDATION_ERROR("Password must be between 6 and 20 symbols long and without white spaces.");

    private final String message;

    messages(String message) {
        this.message = message;
    }

    public String getMessage() {
        return message;
    }
}
