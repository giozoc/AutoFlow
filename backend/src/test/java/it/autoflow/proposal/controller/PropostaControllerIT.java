package it.autoflow.proposal.controller;

import it.autoflow.configuration.entity.Configurazione;
import it.autoflow.configuration.repository.ConfigurazioneRepository;
import it.autoflow.proposal.entity.StatoProposta;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.entity.Ruolo;
import it.autoflow.user.repository.ClienteRepository;
import it.autoflow.vehicle.entity.StatoVeicolo;
import it.autoflow.vehicle.entity.Veicolo;
import it.autoflow.vehicle.repository.VeicoloRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;

import static org.hamcrest.Matchers.is;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test di integrazione TC4 sul CONTROLLER /api/proposte.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class PropostaControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private ConfigurazioneRepository configurazioneRepository;

    @Autowired
    private VeicoloRepository veicoloRepository;

    // --------------------------------------------------------
    // TC4_01 (controller) - POST /api/proposte con dati validi
    // --------------------------------------------------------
    @Test
    void tc4_01_createPropostaValida_restuisce201EJson() throws Exception {
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

        // Veicolo DISPONIBILE
        Veicolo v = new Veicolo();
        v.setMarca("Fiat");
        v.setModello("Panda");
        v.setPrezzoBase(15000.0);
        v.setStato(StatoVeicolo.DISPONIBILE);
        v.setVisibileAlPubblico(true);
        v = veicoloRepository.save(v);

        // Configurazione
        Configurazione conf = new Configurazione();
        conf.setCliente(cliente);
        conf.setVeicolo(v);
        conf.setPrezzoBase(15000.0);
        conf.setPrezzoTotale(17000.0);
        conf.setDataCreazione(LocalDateTime.now());
        conf.setUltimaModifica(LocalDateTime.now());
        conf = configurazioneRepository.save(conf);

        String json = """
            {
              "clienteId": %d,
              "addettoVenditeId": null,
              "configurazioneId": %d,
              "prezzoProposta": 17000.0,
              "noteCliente": "Note cliente",
              "noteInterne": "Note interne",
              "stato": "%s",
              "dataCreazione": "2025-01-10T10:00:00",
              "dataScadenza": "2025-02-10T10:00:00"
            }
            """.formatted(cliente.getId(), conf.getId(), StatoProposta.BOZZA.name());

        mockMvc.perform(post("/api/proposte")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isCreated())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.clienteId").value(cliente.getId()))
                .andExpect(jsonPath("$.configurazioneId").value(conf.getId()))
                .andExpect(jsonPath("$.prezzoProposta").value(17000.0))
                .andExpect(jsonPath("$.stato", is(StatoProposta.BOZZA.name())));
    }

    // --------------------------------------------------------
    // TC4_02 (controller) - POST /api/proposte con cliente inesistente -> 5xx
    // --------------------------------------------------------
    // --------------------------------------------------------
    // TC4_02 (controller) - POST /api/proposte con cliente inesistente
    // Aspettiamo che venga sollevata una ServletException
    // la cui causa è EntityNotFoundException("Cliente non trovato")
    // --------------------------------------------------------
    @Test
    void tc4_02_createProposta_conClienteInesistente_lanciaEntityNotFoundException() {
        String json = """
            {
              "clienteId": 999,
              "addettoVenditeId": null,
              "configurazioneId": 1,
              "prezzoProposta": 20000.0,
              "stato": "BOZZA"
            }
            """;

        jakarta.servlet.ServletException ex = assertThrows(
                jakarta.servlet.ServletException.class,
                () -> mockMvc.perform(post("/api/proposte")
                                .contentType(MediaType.APPLICATION_JSON)
                                .content(json))
                        .andReturn()
        );

        // Verifichiamo che la causa sia proprio "Cliente non trovato"
        Throwable cause = ex.getCause();
        assertNotNull(cause);
        assertTrue(cause instanceof jakarta.persistence.EntityNotFoundException);
        assertEquals("Cliente non trovato", cause.getMessage());
    }
}