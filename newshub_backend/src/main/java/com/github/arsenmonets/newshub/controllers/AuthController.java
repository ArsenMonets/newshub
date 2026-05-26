package com.github.arsenmonets.newshub.controllers;

import com.github.arsenmonets.newshub.dto.AuthResponseDTO;
import com.github.arsenmonets.newshub.dto.LoginDTO;
import com.github.arsenmonets.newshub.dto.RegisterRequestDTO;
import com.github.arsenmonets.newshub.services.AuthService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
    }

    @PostMapping("/register")
    @ResponseStatus(HttpStatus.CREATED)
    public AuthResponseDTO register(@Valid @RequestBody RegisterRequestDTO data) {
        return authService.register(data);
    }

    @PostMapping("/login")
    @ResponseStatus(HttpStatus.OK)
    public AuthResponseDTO login(@Valid @RequestBody LoginDTO credentials) {
        return authService.authenticate(credentials);
    }
}
