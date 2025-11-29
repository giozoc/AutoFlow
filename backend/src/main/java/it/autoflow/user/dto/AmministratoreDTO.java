package it.autoflow.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class AmministratoreDTO {

    private Long id;

    @NotBlank(message = "Il nome è obbligatorio.")
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio.")
    private String cognome;

    @NotBlank(message = "L'email è obbligatoria.")
    @Email(message = "Formato email non valido.")
    private String email;

    private boolean attivo;
}