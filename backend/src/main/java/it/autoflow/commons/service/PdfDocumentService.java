package it.autoflow.commons.service;

import it.autoflow.commons.entity.DocumentoPDF;

/**
 * Servizio per la generazione e rigenerazione dei PDF
 * (preventivi, proposte, fatture) come descritto nell'ODD.
 */
public interface PdfDocumentService {

    /**
     * Genera il PDF associato a una proposta.
     *
     * @param propostaId id della proposta
     * @return entità DocumentoPDF persistita con path del file generato
     */
    DocumentoPDF generateProposalPdf(Long propostaId);

    /**
     * Genera il PDF associato a una fattura.
     *
     * @param fatturaId id della fattura
     * @return entità DocumentoPDF persistita con path del file generato
     */
    DocumentoPDF generateInvoicePdf(Long fatturaId);

    /**
     * Rigenera un PDF già esistente (es. dopo modifica dati)
     *
     * @param documentoId id del DocumentoPDF
     * @return entità DocumentoPDF aggiornata
     */
    DocumentoPDF regeneratePdf(Long documentoId);
}