package it.autoflow.invoice.service;

import it.autoflow.commons.entity.DocumentoPDF;
import it.autoflow.commons.repository.DocumentoPDFRepository;
import it.autoflow.commons.service.PdfDocumentService;
import it.autoflow.configuration.entity.Configurazione;
import it.autoflow.configuration.repository.ConfigurazioneRepository;
import it.autoflow.invoice.dto.FatturaDTO;
import it.autoflow.invoice.entity.Fattura;
import it.autoflow.invoice.repository.FatturaRepository;
import it.autoflow.proposal.entity.Proposta;
import it.autoflow.proposal.entity.StatoProposta;
import it.autoflow.proposal.repository.PropostaRepository;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.entity.Ruolo;
import it.autoflow.user.repository.ClienteRepository;
import it.autoflow.vehicle.entity.StatoVeicolo;
import it.autoflow.vehicle.entity.Veicolo;
import it.autoflow.vehicle.repository.VeicoloRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verifyNoInteractions;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class FatturaServiceIT {

    @Autowired
    private FatturaService fatturaService;

    @Autowired
    private FatturaRepository fatturaRepository;

    @Autowired
    private PropostaRepository propostaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ConfigurazioneRepository configurazioneRepository;

    @Autowired
    private VeicoloRepository veicoloRepository;

    @Autowired
    private DocumentoPDFRepository documentoPDFRepository;

    // mockiamo il servizio PDF per non toccare il filesystem
    @MockBean
    private PdfDocumentService pdfDocumentService;

    // ----------------- helper per configurazione valida -----------------

    private Configurazione creaConfigurazionePerCliente(Cliente cliente) {
        Veicolo v = new Veicolo();
        v.setMarca("Fiat");
        v.setModello("Panda");
        v.setPrezzoBase(15000.0);
        v.setStato(StatoVeicolo.DISPONIBILE);
        v.setVisibileAlPubblico(true);
        v = veicoloRepository.save(v);

        Configurazione conf = new Configurazione();
        conf.setCliente(cliente);
        conf.setVeicolo(v);
        conf.setPrezzoBase(15000.0);
        conf.setPrezzoTotale(16000.0);
        conf.setDataCreazione(LocalDateTime.now());
        conf.setUltimaModifica(LocalDateTime.now());
        return configurazioneRepository.save(conf);
    }

    // ---------------------------------------------------
    // TC5_01: PE1, PS1, FS1
    // ORACOLO: Fattura generata e salvata correttamente.
    // ---------------------------------------------------
    @Test
    void tc5_01_createFromProposta_valida_generataESalvataCorrettamente() {
        int year = LocalDate.now().getYear();
        String prefix = "AF-" + year + "-";

        // Cliente
        Cliente cliente = new Cliente();
        cliente.setNome("Chiara");
        cliente.setCognome("Verdi");
        cliente.setEmail("chiara.verdi@example.com");
        cliente.setUsername("chiara.verdi@example.com");
        cliente.setPassword("pwd");
        cliente.setRuolo(Ruolo.CLIENTE);
        cliente.setAttivo(true);
        cliente = clienteRepository.save(cliente);

        // Configurazione associata
        Configurazione conf = creaConfigurazionePerCliente(cliente);

        // Proposta ACCETTATA
        Proposta p = new Proposta();
        p.setCliente(cliente);
        p.setConfigurazione(conf);
        p.setPrezzoProposta(25_000.0);
        p.setStato(StatoProposta.ACCETTATA);
        p.setDataCreazione(LocalDateTime.now());
        p = propostaRepository.save(p);

        // mock PdfDocumentService:
        // genera e SALVA un DocumentoPDF reale su H2,
        // così la FK documento_pdf_id punta a un record esistente
        given(pdfDocumentService.generateInvoicePdf(anyLong()))
                .willAnswer(invocation -> {
                    DocumentoPDF doc = new DocumentoPDF();
                    doc.setNomeFile("fattura-test.pdf");
                    doc.setPath("/tmp/fattura-test.pdf"); // basta che non sia null
                    doc.setDimensioneBytes(12345L);
                    doc.setDataCreazione(LocalDateTime.now());
                    doc.setUltimaModifica(LocalDateTime.now());
                    return documentoPDFRepository.save(doc);
                });

        // act
        FatturaDTO result = fatturaService.createFromProposta(p.getId());

        // assert DTO
        assertNotNull(result);
        assertNotNull(result.getId());
        assertTrue(result.getNumeroFattura().startsWith(prefix));
        assertEquals(cliente.getId(), result.getClienteId());
        assertEquals(p.getId(), result.getPropostaId());
        assertEquals(25_000.0, result.getImportoTotale());

        assertNotNull(result.getDocumentoPdfId());
        assertTrue(result.getDocumentoPdfId() > 0);
        assertTrue(documentoPDFRepository.findById(result.getDocumentoPdfId()).isPresent());

        // assert DB
        List<Fattura> all = fatturaRepository.findAll();
        assertEquals(1, all.size());
        Fattura salvata = all.get(0);
        assertEquals(result.getId(), salvata.getId());
        assertEquals(cliente.getId(), salvata.getCliente().getId());
        assertEquals(p.getId(), salvata.getProposta().getId());
        assertNotNull(salvata.getDocumentoPdf());
    }

    // ---------------------------------------------------
    // TC5_02: PE2, PS1, FS1
    // ORACOLO: Errore: "Proposta non trovata".
    // ---------------------------------------------------
    @Test
    void tc5_02_propostaNonEsistente_lanciaEccezionePropostaNonTrovata() {
        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> fatturaService.createFromProposta(999L)
        );

        assertEquals("Proposta non trovata", ex.getMessage());
        assertEquals(0, fatturaRepository.count());
        verifyNoInteractions(pdfDocumentService);
    }

    // ---------------------------------------------------
    // TC5_03: PE1, PS2, FS1
    // ORACOLO: Errore: "La proposta non è confermata".
    // ---------------------------------------------------
    @Test
    void tc5_03_propostaNonConfermata_lanciaEccezioneStatoNonValido() {
        // Cliente
        Cliente cliente = new Cliente();
        cliente.setNome("Mario");
        cliente.setCognome("Rossi");
        cliente.setEmail("mario.rossi@example.com");
        cliente.setUsername("mario.rossi@example.com");
        cliente.setPassword("pwd");
        cliente.setRuolo(Ruolo.CLIENTE);
        cliente.setAttivo(true);
        cliente = clienteRepository.save(cliente);

        // Configurazione
        Configurazione conf = creaConfigurazionePerCliente(cliente);

        // Proposta NON confermata
        Proposta p = new Proposta();
        p.setCliente(cliente);
        p.setConfigurazione(conf);
        p.setPrezzoProposta(20_000.0);
        p.setStato(StatoProposta.BOZZA);   // 👈 non ACCETTATA
        p.setDataCreazione(LocalDateTime.now());
        p = propostaRepository.save(p);

        final Long propostaId = p.getId();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> fatturaService.createFromProposta(propostaId)
        );

        assertEquals("La proposta non è confermata", ex.getMessage());
        assertEquals(0, fatturaRepository.count());
        verifyNoInteractions(pdfDocumentService);
    }

    // ---------------------------------------------------
    // TC5_04: PE1, PS1, FS2
    // ORACOLO: Errore:
    // "Impossibile generare il PDF – spazio insufficiente".
    // ---------------------------------------------------
    @Test
    void tc5_04_spazioInsufficienteDuranteGenerazionePdf_lanciaEccezione() {
        // Cliente
        Cliente cliente = new Cliente();
        cliente.setNome("Laura");
        cliente.setCognome("Neri");
        cliente.setEmail("laura.neri@example.com");
        cliente.setUsername("laura.neri@example.com");
        cliente.setPassword("pwd");
        cliente.setRuolo(Ruolo.CLIENTE);
        cliente.setAttivo(true);
        cliente = clienteRepository.save(cliente);

        // Configurazione
        Configurazione conf = creaConfigurazionePerCliente(cliente);

        // Proposta ACCETTATA
        Proposta p = new Proposta();
        p.setCliente(cliente);
        p.setConfigurazione(conf);
        p.setPrezzoProposta(30_000.0);
        p.setStato(StatoProposta.ACCETTATA);
        p.setDataCreazione(LocalDateTime.now());
        p = propostaRepository.save(p);

        // PdfDocumentService che fallisce
        given(pdfDocumentService.generateInvoicePdf(anyLong()))
                .willThrow(new IllegalStateException("Impossibile generare il PDF – spazio insufficiente"));

        final Long propostaId = p.getId();

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> fatturaService.createFromProposta(propostaId)
        );

        assertEquals("Impossibile generare il PDF – spazio insufficiente", ex.getMessage());
        // niente assert sul DB: dipende dalla transazione se la fattura viene salvata o rollbackata
    }
}