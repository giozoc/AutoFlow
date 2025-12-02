package it.autoflow.proposal.dto;

import it.autoflow.proposal.entity.StatoProposta;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PropostaDTO {

    private Long id;

    @NotNull
    private Long clienteId;

    // 👇 NIENTE @NotNull QUI
    private Long addettoVenditeId;

    @NotNull
    private Long configurazioneId;

    @NotNull
    private Double prezzoProposta;

    private String noteCliente;
    private String noteInterne;

    private StatoProposta stato;
    private LocalDateTime dataCreazione;
    private LocalDateTime dataScadenza;
}