package it.autoflow.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import lombok.Data;

import java.time.LocalDate;

@Data
public class ClienteDTO {

    private Long id;

    @NotBlank(message = "Il nome è obbligatorio.")
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio.")
    private String cognome;

    @NotBlank(message = "L'email è obbligatoria.")
    @Email(message = "Formato email non valido.")
    private String email;

    @Pattern(regexp = "^[0-9\\+ ]{7,20}$", message = "Numero di telefono non valido.")
    private String telefono;

    @NotBlank(message = "L'indirizzo è obbligatorio.")
    private String indirizzo;

    private boolean attivo;

    @NotBlank(message = "Il CF è obbligatorio.")
    private String codiceFiscale;

    @NotNull(message = "La data di nascita è obbligatoria.")
    private LocalDate dataNascita;
}