package it.autoflow.authentication.controller;

import it.autoflow.authentication.dto.RegisterClienteDTO;
import it.autoflow.authentication.service.AuthenticationService;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/auth/register")
@RequiredArgsConstructor
public class RegisterController {

    private final AuthenticationService authService;

    @PostMapping("/cliente")
    public void registerCliente(@RequestBody RegisterClienteDTO dto) {
        authService.registerCliente(dto);
    }
}