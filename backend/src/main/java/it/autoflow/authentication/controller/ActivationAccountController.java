package it.autoflow.authentication.controller;

import it.autoflow.authentication.dto.ActivationRequestDTO;
import it.autoflow.authentication.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/activate")
@RequiredArgsConstructor
public class ActivationAccountController {

    private final AuthenticationService authService;

    @PostMapping
    public boolean activate(@RequestBody ActivationRequestDTO dto) {
        return authService.activateAccount(dto.getToken());
    }
}