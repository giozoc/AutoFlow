package it.autoflow.configuration.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.Set;

@Data
public class ConfigurazioneDTO {

    private Long id;

    @NotNull(message = "Il cliente è obbligatorio.")
    private Long clienteId;

    @NotNull(message = "Il veicolo è obbligatorio.")
    private Long veicoloId;

    @NotNull(message = "Il prezzo base è obbligatorio.")
    @PositiveOrZero(message = "Il prezzo base non può essere negativo.")
    private Double prezzoBase;

    @NotNull(message = "Il prezzo totale è obbligatorio.")
    @PositiveOrZero(message = "Il prezzo totale non può essere negativo.")
    private Double prezzoTotale;

    private LocalDateTime dataCreazione;

    @Size(max = 500, message = "Le note non possono superare 500 caratteri.")
    private String note;

    private Set<Long> optionalIds;
}