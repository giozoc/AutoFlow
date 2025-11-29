package it.autoflow.configuration.dto;


import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class OptionalAccessorioDTO {

    private Long id;

    @NotBlank(message = "Il codice è obbligatorio.")
    private String codice;

    @NotBlank(message = "Il nome è obbligatorio.")
    private String nome;

    @Size(max = 500, message = "La descrizione può contenere al massimo 500 caratteri.")
    private String descrizione;

    @NotNull(message = "Il prezzo è obbligatorio.")
    @PositiveOrZero(message = "Il prezzo non può essere negativo.")
    private Double prezzo;
}