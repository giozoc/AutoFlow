package it.autoflow.invoice.service;

import it.autoflow.commons.entity.DocumentoPDF;
import it.autoflow.commons.repository.DocumentoPDFRepository;
import it.autoflow.commons.service.PdfDocumentService;
import it.autoflow.invoice.dto.FatturaDTO;
import it.autoflow.invoice.entity.Fattura;
import it.autoflow.invoice.repository.FatturaRepository;
import it.autoflow.proposal.entity.Proposta;
import it.autoflow.proposal.entity.StatoProposta;
import it.autoflow.proposal.repository.PropostaRepository;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.repository.ClienteRepository;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class FatturaServiceImplTest {

    @Mock
    private FatturaRepository fatturaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private PropostaRepository propostaRepository;

    @Mock
    private DocumentoPDFRepository documentoPDFRepository;

    @Mock
    private PdfDocumentService pdfDocumentService;

    @InjectMocks
    private FatturaServiceImpl fatturaService;

    // ---------------------------------------------------
    // TC5_01: PE1, PS1, FS1
    // ORACOLO: Fattura generata e salvata correttamente.
    // ---------------------------------------------------
    @Test
    void tc5_01_createFromProposta_valida_generataESalvataCorrettamente() {
        // anno corrente (serve per il numero fattura)
        int year = LocalDate.now().getYear();
        String prefix = "AF-" + year + "-";

        // ---- Proposta valida, confermata ----
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Proposta proposta = new Proposta();
        proposta.setId(10L);
        proposta.setCliente(cliente);
        proposta.setPrezzoProposta(25_000.0);
        proposta.setStato(StatoProposta.ACCETTATA);
        proposta.setDataCreazione(LocalDateTime.now());

        // repository proposta (prima chiamata + quella dentro create())
        given(propostaRepository.findById(10L)).willReturn(Optional.of(proposta));

        // numerazione fattura: nessuna fattura precedente
        given(fatturaRepository.findTopByNumeroFatturaStartingWithOrderByNumeroFatturaDesc(prefix))
                .willReturn(null);

        // repository cliente (usato dentro create())
        given(clienteRepository.findById(1L)).willReturn(Optional.of(cliente));

        // save fattura → assegniamo un id
        when(fatturaRepository.save(any(Fattura.class))).thenAnswer(invocation -> {
            Fattura f = invocation.getArgument(0);
            if (f.getId() == null) {
                f.setId(100L);
            }
            return f;
        });

        // findById dopo create(), usato in createFromProposta
        given(fatturaRepository.findById(100L)).willReturn(
                Optional.of(new Fattura() {{
                    setId(100L);
                    setNumeroFattura(prefix + "001");
                    setDataEmissione(LocalDate.now());
                    setCliente(cliente);
                    setProposta(proposta);
                    setImportoTotale(25_000.0);
                }})
        );

        // PDF generato correttamente
        DocumentoPDF documentoPDF = new DocumentoPDF();
        documentoPDF.setId(999L);
        given(pdfDocumentService.generateInvoicePdf(100L)).willReturn(documentoPDF);

        // ---- esecuzione ----
        FatturaDTO result = fatturaService.createFromProposta(10L);

        // ---- asserzioni ----
        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(prefix + "001", result.getNumeroFattura());
        assertEquals(1L, result.getClienteId());
        assertEquals(10L, result.getPropostaId());
        assertEquals(25_000.0, result.getImportoTotale());
        assertEquals(999L, result.getDocumentoPdfId());

        // verifichiamo che il PDF sia stato generato
        verify(pdfDocumentService).generateInvoicePdf(100L);
    }

    // ---------------------------------------------------
    // TC5_02: PE2, PS1, FS1
    // ORACOLO: Errore: "Proposta non trovata".
    // ---------------------------------------------------
    @Test
    void tc5_02_propostaNonEsistente_lanciaEccezionePropostaNonTrovata() {
        given(propostaRepository.findById(999L)).willReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> fatturaService.createFromProposta(999L)
        );

        assertEquals("Proposta non trovata", ex.getMessage());
    }

    // ---------------------------------------------------
    // TC5_03: PE1, PS2, FS1
    // ORACOLO: Errore: "La proposta non è confermata".
    // ---------------------------------------------------
    @Test
    void tc5_03_propostaNonConfermata_lanciaEccezioneStatoNonValido() {
        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Proposta proposta = new Proposta();
        proposta.setId(10L);
        proposta.setCliente(cliente);
        proposta.setPrezzoProposta(20_000.0);
        proposta.setStato(StatoProposta.BOZZA); // NON confermata

        given(propostaRepository.findById(10L)).willReturn(Optional.of(proposta));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> fatturaService.createFromProposta(10L)
        );

        assertEquals("La proposta non è confermata", ex.getMessage());
    }

    // ---------------------------------------------------
    // TC5_04: PE1, PS1, FS2
    // ORACOLO: Errore:
    // "Impossibile generare il PDF – spazio insufficiente".
    // ---------------------------------------------------
    @Test
    void tc5_04_spazioInsufficienteDuranteGenerazionePdf_lanciaEccezione() {
        int year = LocalDate.now().getYear();
        String prefix = "AF-" + year + "-";

        Cliente cliente = new Cliente();
        cliente.setId(1L);

        Proposta proposta = new Proposta();
        proposta.setId(10L);
        proposta.setCliente(cliente);
        proposta.setPrezzoProposta(30_000.0);
        proposta.setStato(StatoProposta.ACCETTATA);

        given(propostaRepository.findById(10L)).willReturn(Optional.of(proposta));
        given(fatturaRepository.findTopByNumeroFatturaStartingWithOrderByNumeroFatturaDesc(prefix))
                .willReturn(null);
        given(clienteRepository.findById(1L)).willReturn(Optional.of(cliente));

        when(fatturaRepository.save(any(Fattura.class))).thenAnswer(invocation -> {
            Fattura f = invocation.getArgument(0);
            if (f.getId() == null) {
                f.setId(200L);
            }
            return f;
        });

        //given(fatturaRepository.findById(200L)).willReturn(Optional.of(new Fattura()));

        // simuliamo errore del File System tramite PdfDocumentService
        given(pdfDocumentService.generateInvoicePdf(200L))
                .willThrow(new IllegalStateException("Impossibile generare il PDF – spazio insufficiente"));

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> fatturaService.createFromProposta(10L)
        );

        assertEquals("Impossibile generare il PDF – spazio insufficiente", ex.getMessage());
    }
}