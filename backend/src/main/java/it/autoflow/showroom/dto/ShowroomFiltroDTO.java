package it.autoflow.showroom.dto;

import jakarta.validation.constraints.PositiveOrZero;
import lombok.Data;

@Data
public class ShowroomFiltroDTO {

    private String marca;
    private String modello;

    @PositiveOrZero(message = "Il prezzo minimo non può essere negativo.")
    private Double prezzoMin;

    @PositiveOrZero(message = "Il prezzo massimo non può essere negativo.")
    private Double prezzoMax;
}