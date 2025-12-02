package it.autoflow.authentication.dto;

import jakarta.validation.constraints.*;
import lombok.Data;

import java.time.LocalDate;

@Data
public class RegisterClienteDTO {

    @NotBlank(message = "Il nome è obbligatorio.")
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio.")
    private String cognome;

    @NotBlank(message = "L'email è obbligatoria.")
    @Email(message = "Formato email non valido.")
    private String email;

    @NotBlank(message = "La password è obbligatoria.")
    @Size(min = 8, message = "La password deve contenere almeno 8 caratteri.")
    private String password;

    @NotBlank(message = "Il numero di telefono è obbligatorio.")
    @Pattern(regexp = "^[0-9\\+ ]{7,20}$", message = "Numero di telefono non valido.")
    private String telefono;

    @NotBlank(message = "L'indirizzo è obbligatorio.")
    private String indirizzo;

    @NotBlank(message = "Il CF è obbligatorio.")
    private String codiceFiscale;

    @NotNull(message = "La data di nascita è obbligatoria.")
    private LocalDate dataNascita;
}