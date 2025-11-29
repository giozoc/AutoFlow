package it.autoflow.invoice.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

import java.time.LocalDate;

@Data
public class FatturaDTO {

    private Long id;

    @NotBlank(message = "Il numero della fattura è obbligatorio.")
    private String numeroFattura;

    @NotNull(message = "La data di emissione è obbligatoria.")
    private LocalDate dataEmissione;

    @NotNull(message = "Il cliente è obbligatorio.")
    private Long clienteId;

    @NotNull(message = "La proposta associata è obbligatoria.")
    private Long propostaId;

    @NotNull(message = "L'importo totale è obbligatorio.")
    @PositiveOrZero(message = "L'importo totale non può essere negativo.")
    private Double importoTotale;

    private LocalDate dataPagamento;

    private Long documentoPdfId;
}