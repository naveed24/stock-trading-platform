package com.example.auth_service.dto;


import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@AllArgsConstructor
public class AuthResponse {

    private String message;

    private String accessToken;

    private String tokenType;

    private Long userId;

    private String email;

    private String role;
}
