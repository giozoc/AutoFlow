package it.autoflow.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class PasswordFirstChangeDTO {

    @NotBlank(message = "Il token di sessione è obbligatorio.")
    private String token;          // token di login restituito dal backend

    @NotBlank(message = "La nuova password è obbligatoria.")
    @Size(min = 8, message = "La password deve contenere almeno 8 caratteri.")
    private String nuovaPassword;
}
