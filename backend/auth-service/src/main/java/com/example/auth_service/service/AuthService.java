package com.example.auth_service.service;

import com.example.auth_service.dto.AuthResponse;
import com.example.auth_service.dto.LoginRequest;
import com.example.auth_service.dto.RegisterRequest;
import com.example.auth_service.entity.User;
import com.example.auth_service.enums.Role;
import com.example.auth_service.repository.UserRepository;
import com.example.auth_service.security.JwtService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AuthService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request){
        if(userRepository.existsByEmail(request.getEmail())){
            throw new RuntimeException("Email already registered");
        }

        User user= User.builder()
                .name(request.getName())
                .email(request.getEmail())
                .password((passwordEncoder.encode(request.getPassword())))
                .role(Role.USER)
                .build();

        User savedUser=userRepository.save(user);

        String token= jwtService.generateToken(savedUser);

        return new AuthResponse("User registered successfully",token,"Bearer", savedUser.getId(), savedUser.getEmail(), savedUser.getRole().name());

    }

    public AuthResponse login(LoginRequest request){
        User user=userRepository.findByEmail(request.getEmail()).orElseThrow(()-> new RuntimeException("Invalid email or password"));

        if(!passwordEncoder.matches(request.getPassword(), user.getPassword())){
            throw new RuntimeException("Invalid email or password");
        }

        String token= jwtService.generateToken(user);

        return new AuthResponse("Login successful",token,"Bearer",user.getId(), user.getEmail(), user.getRole().name());
    }
}
