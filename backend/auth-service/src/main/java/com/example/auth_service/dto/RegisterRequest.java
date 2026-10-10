package com.example.auth_service.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;


@Getter
@Setter
public class RegisterRequest {

    @NotBlank(message ="Name is required")
    private String name;

    @Email(message = "Invalid email format")
    @NotBlank(message="Email is required")
    private String email;

    @NotBlank(message="Password is required")
    @Size(min=8, message="Password must contain at least 8 characters")
    private String password;

}
