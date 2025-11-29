package it.autoflow.authentication.controller;

import it.autoflow.authentication.dto.LoginRequestDTO;
import it.autoflow.authentication.dto.LoginResponseDTO;
import it.autoflow.authentication.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
public class AuthenticationController {

    private final AuthenticationService authService;

    @PostMapping("/login")
    public LoginResponseDTO login(@RequestBody LoginRequestDTO request) {
        return authService.login(request);
    }

    @PostMapping("/logout")
    public boolean logout(@RequestHeader("Authorization") String token) {
        return authService.logout(token);
    }
}