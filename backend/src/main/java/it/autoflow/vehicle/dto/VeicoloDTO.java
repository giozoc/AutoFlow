package it.autoflow.vehicle.dto;

import it.autoflow.vehicle.entity.StatoVeicolo;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;



@Data
public class VeicoloDTO {

    private Long id;

    @NotBlank(message = "La marca è obbligatoria.")
    private String marca;

    @NotBlank(message = "Il modello è obbligatorio.")
    private String modello;

    @NotNull(message = "L'anno è obbligatorio.")
    @Min(value = 1900, message = "Anno non valido.")
    private Integer anno;

    @NotBlank(message = "La targa è obbligatoria.")
    private String targa;

    @NotBlank(message = "Il VIN è obbligatorio.")
    private String vin;

    @NotNull(message = "Il prezzo base è obbligatorio.")
    @PositiveOrZero(message = "Il prezzo base non può essere negativo.")
    private Double prezzoBase;

    @PositiveOrZero(message = "Il chilometraggio non può essere negativo.")
    private Integer chilometraggio;

    @NotBlank(message = "L'alimentazione è obbligatoria.")
    private String alimentazione;

    @NotBlank(message = "Il tipo di cambio è obbligatorio.")
    private String cambio;

    @NotBlank(message = "Il colore esterno è obbligatorio.")
    private String coloreEsterno;

    @NotNull(message = "Lo stato del veicolo è obbligatorio.")
    private StatoVeicolo stato;

    private boolean visibileAlPubblico;
}