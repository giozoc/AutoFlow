package it.autoflow.proposal.dto;

import it.autoflow.proposal.entity.StatoProposta;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;

@Data
public class PropostaDTO {

    private Long id;

    @NotNull(message = "Il cliente è obbligatorio.")
    private Long clienteId;

    @NotNull(message = "L'addetto vendite è obbligatorio.")
    private Long addettoVenditeId;

    @NotNull(message = "La configurazione è obbligatoria.")
    private Long configurazioneId;

    @NotNull(message = "Il prezzo proposta è obbligatorio.")
    @PositiveOrZero(message = "Il prezzo proposta non può essere negativo.")
    private Double prezzoProposta;

    private StatoProposta stato;

    private LocalDate dataCreazione;
    private LocalDate dataScadenza;

    @Size(max = 500, message = "Le note cliente possono contenere al massimo 500 caratteri.")
    private String noteCliente;

    @Size(max = 500, message = "Le note interne possono contenere al massimo 500 caratteri.")
    private String noteInterne;
}