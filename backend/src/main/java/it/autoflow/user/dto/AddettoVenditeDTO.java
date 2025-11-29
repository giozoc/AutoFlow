package it.autoflow.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AddettoVenditeDTO {

    private Long id;

    @NotBlank(message = "Il nome è obbligatorio.")
    private String nome;

    @NotBlank(message = "Il cognome è obbligatorio.")
    private String cognome;

    @NotBlank(message = "La matricola è obbligatoria.")
    private String matricola;

    @NotBlank(message = "L'email è obbligatoria.")
    @Email(message = "Formato email non valido.")
    private String email;

    @NotBlank(message = "Il telefono è obbligatorio.")
    @Pattern(regexp = "^[0-9\\+ ]{7,20}$", message = "Numero di telefono non valido.")
    private String telefono;

    @NotBlank(message = "La password è obbligatoria.")
    @Size(min = 8, message = "La password deve avere almeno 8 caratteri.")
    private String password;

    private boolean attivo;

    @NotBlank(message = "Il ruolo è obbligatorio.")
    private String ruolo;
}