package it.autoflow.user.controller;

import it.autoflow.authentication.utils.PasswordHasher;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.entity.Ruolo;
import it.autoflow.user.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test di integrazione TC2 sul CONTROLLER /clienti.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
@TestPropertySource(properties = {
        "spring.jpa.hibernate.ddl-auto=create-drop",
        "spring.sql.init.mode=never"
})
class ClienteControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    // --------------------------------------------------------
    // TC2_01 (versione controller) - PUT /clienti/{id} profilo valido
    // --------------------------------------------------------
    @Test
    void tc2_01_updateClienteValido_restuisce200EProfiloAggiornato() throws Exception {
        Cliente c = new Cliente();
        c.setUsername("chiara.verdi@example.com");
        c.setPassword(PasswordHasher.sha512("AutoFlow2025!"));
        c.setRuolo(Ruolo.CLIENTE);
        c.setAttivo(true);
        c.setNome("Chiara");
        c.setCognome("Verdi");
        c.setEmail("chiara.verdi@example.com");
        c.setTelefono("3331112222");
        c.setIndirizzo("Via Vecchia 1");
        c.setCodiceFiscale("VRDCHR90B02H501U");
        c.setDataNascita(LocalDate.of(1990, 2, 2));
        c = clienteRepository.save(c);

        Long id = c.getId();

        String requestJson = """
            {
              "id": %d,
              "nome": "Chiara",
              "cognome": "Verdi",
              "email": "chiara.verdi@example.com",
              "telefono": "3339998888",
              "indirizzo": "Via Nuova 10",
              "attivo": true,
              "codiceFiscale": "VRDCHR90B02H501U",
              "dataNascita": "1990-02-02"
            }
            """.formatted(id);

        mockMvc.perform(put("/clienti/{id}", id)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(requestJson))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.telefono").value("3339998888"))
                .andExpect(jsonPath("$.indirizzo").value("Via Nuova 10"));

        // Verifica anche sul DB
        Cliente updated = clienteRepository.findById(id).orElseThrow();
        org.junit.jupiter.api.Assertions.assertEquals("3339998888", updated.getTelefono());
        org.junit.jupiter.api.Assertions.assertEquals("Via Nuova 10", updated.getIndirizzo());
    }

    // --------------------------------------------------------
    // TC2_02 (controller) - GET /clienti/{id} cliente esistente
    // --------------------------------------------------------
    @Test
    void tc2_02_getClienteById_restuisce200EProfilo() throws Exception {
        Cliente c = new Cliente();
        c.setUsername("mario.rossi@example.com");
        c.setPassword(PasswordHasher.sha512("Mario123!"));
        c.setRuolo(Ruolo.CLIENTE);
        c.setAttivo(true);
        c.setNome("Mario");
        c.setCognome("Rossi");
        c.setEmail("mario.rossi@example.com");
        c.setTelefono("3334445555");
        c.setIndirizzo("Via Roma 5");
        c.setCodiceFiscale("RSSMRA80A01H501Z");
        c.setDataNascita(LocalDate.of(1980, 1, 1));
        c = clienteRepository.save(c);

        Long id = c.getId();

        mockMvc.perform(get("/clienti/{id}", id))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(id))
                .andExpect(jsonPath("$.nome", is("Mario")))
                .andExpect(jsonPath("$.cognome", is("Rossi")))
                .andExpect(jsonPath("$.email", is("mario.rossi@example.com")));
    }
}