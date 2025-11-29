package it.autoflow.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordResetConfirmDTO {

    @NotBlank(message = "Il token è obbligatorio.")
    private String token;

    @NotBlank(message = "La nuova password è obbligatoria.")
    @Size(min = 8, message = "La password deve contenere almeno 8 caratteri.")
    private String nuovaPassword;
}