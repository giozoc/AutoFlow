package it.autoflow.authentication.dto;

import lombok.Data;

@Data
public class LoginResponseDTO {
    private String token;
    private String ruolo;        // CLIENTE / ADDETTO / ADMIN
    private Long userId;
    private boolean mustChangePassword;   // 👈 nuovo campo
}
