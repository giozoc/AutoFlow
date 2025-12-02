package it.autoflow.authentication.controller;

import it.autoflow.authentication.dto.PasswordFirstChangeDTO;
import it.autoflow.authentication.dto.PasswordResetRequestDTO;
import it.autoflow.authentication.dto.PasswordResetConfirmDTO;
import it.autoflow.authentication.dto.PasswordResetResultDTO;
import it.autoflow.authentication.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/password")
@RequiredArgsConstructor
public class PasswordRecoveryController {

    private final AuthenticationService authService;

    @PostMapping("/reset-request")
    public PasswordResetResultDTO requestReset(@RequestBody PasswordResetRequestDTO dto) {
        return authService.requestPasswordReset(dto.getEmail());
    }

    // conferma via token rimane se mai ti servirà in futuro
    @PostMapping("/reset-confirm")
    public boolean confirmReset(@RequestBody PasswordResetConfirmDTO dto) {
        return authService.confirmPasswordReset(dto);
    }

    // NUOVO endpoint per cambio password al primo accesso
    @PostMapping("/first-change")
    public boolean changeAfterReset(@RequestBody PasswordFirstChangeDTO dto) {
        return authService.changePasswordAfterReset(dto);
    }
}
