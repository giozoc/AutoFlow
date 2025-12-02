package it.autoflow.authentication.dto;

import lombok.Data;

@Data
public class PasswordResetResultDTO {
    private boolean success;
    private String defaultPassword; // es. "Cliente123!" o "Addetto123!"
}
