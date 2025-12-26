package it.autoflow.invoice.controller;

import it.autoflow.commons.entity.DocumentoPDF;
import it.autoflow.commons.repository.DocumentoPDFRepository;
import it.autoflow.commons.service.FileStorageService;
import it.autoflow.commons.service.PdfDocumentService;
import it.autoflow.configuration.entity.Configurazione;
import it.autoflow.configuration.repository.ConfigurazioneRepository;
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
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;

import static org.hamcrest.Matchers.greaterThan;
import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class FatturaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private PropostaRepository propostaRepository;

    @Autowired
    private FatturaRepository fatturaRepository;

    @Autowired
    private ConfigurazioneRepository configurazioneRepository;

    @Autowired
    private VeicoloRepository veicoloRepository;

    @Autowired
    private DocumentoPDFRepository documentoPDFRepository;

    // mockati solo per soddisfare il controller e il service
    @MockitoBean
    private PdfDocumentService pdfDocumentService;

    @MockitoBean
    private FileStorageService fileStorageService;

    // ------------ helper configurazione --------------

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

    // --------------------------------------------------------
    // TC5_01 (controller) - POST /api/fatture/da-proposta/{id}
    // --------------------------------------------------------
    @Test
    void tc5_01_createFromPropostaValida_restuisce201EJson() throws Exception {
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

        // Configurazione + Proposta ACCETTATA
        Configurazione conf = creaConfigurazionePerCliente(cliente);

        Proposta p = new Proposta();
        p.setCliente(cliente);
        p.setConfigurazione(conf);
        p.setPrezzoProposta(25_000.0);
        p.setStato(StatoProposta.ACCETTATA);
        p.setDataCreazione(LocalDateTime.now());
        p = propostaRepository.save(p);

        // mock PDF generato
        DocumentoPDF doc = new DocumentoPDF();
        doc.setNomeFile("fattura-777.pdf");
        doc.setPath("/tmp/fattura-777.pdf");
        doc.setDimensioneBytes(12345L);
        doc.setDataCreazione(LocalDateTime.now());
        doc.setUltimaModifica(LocalDateTime.now());
        doc = documentoPDFRepository.save(doc);

        given(pdfDocumentService.generateInvoicePdf(anyLong()))
                .willReturn(doc);

        mockMvc.perform(post("/api/fatture/da-proposta/{propostaId}", p.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id", greaterThan(0)))
                .andExpect(jsonPath("$.clienteId").value(cliente.getId()))
                .andExpect(jsonPath("$.propostaId").value(p.getId()))
                .andExpect(jsonPath("$.importoTotale").value(25_000.0));

        List<Fattura> all = fatturaRepository.findAll();
        assertEquals(1, all.size());
        Fattura f = all.get(0);
        assertEquals(cliente.getId(), f.getCliente().getId());
        assertEquals(p.getId(), f.getProposta().getId());
    }

    // --------------------------------------------------------
    // TC5_02 (controller) - proposta inesistente
    // -> ServletException con causa EntityNotFoundException("Proposta non trovata")
    // --------------------------------------------------------
    @Test
    void tc5_02_createFromPropostaInesistente_lanciaEntityNotFoundException() {
        jakarta.servlet.ServletException ex = assertThrows(
                jakarta.servlet.ServletException.class,
                () -> mockMvc.perform(post("/api/fatture/da-proposta/{propostaId}", 999L)
                                .contentType(MediaType.APPLICATION_JSON))
                        .andReturn()
        );

        Throwable cause = ex.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof EntityNotFoundException);
        assertEquals("Proposta non trovata", cause.getMessage());
        assertEquals(0, fatturaRepository.count());
    }
}