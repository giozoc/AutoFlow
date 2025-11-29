package it.autoflow.authentication.controller;

import it.autoflow.authentication.dto.PasswordResetRequestDTO;
import it.autoflow.authentication.dto.PasswordResetConfirmDTO;
import it.autoflow.authentication.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/password")
@RequiredArgsConstructor
public class PasswordRecoveryController {

    private final AuthenticationService authService;

    @PostMapping("/reset-request")
    public void requestReset(@RequestBody PasswordResetRequestDTO dto) {
        authService.requestPasswordReset(dto.getEmail());
    }

    @PostMapping("/reset-confirm")
    public boolean confirmReset(@RequestBody PasswordResetConfirmDTO dto) {
        return authService.confirmPasswordReset(dto);
    }
}