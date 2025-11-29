package it.autoflow.authentication.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.Data;

@Data
public class ActivationRequestDTO {

    @NotBlank(message = "Il token di attivazione è obbligatorio.")
    private String token;
}