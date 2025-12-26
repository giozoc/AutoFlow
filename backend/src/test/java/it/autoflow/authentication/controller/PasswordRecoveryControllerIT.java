package it.autoflow.authentication.controller;

import it.autoflow.authentication.utils.PasswordHasher;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.entity.Ruolo;
import it.autoflow.user.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;

import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.nullValue;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test di integrazione TC3 sul CONTROLLER /auth/password.
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class PasswordRecoveryControllerIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    // --------------------------------------------------------
    // TC3_01 (controller) - /auth/password/reset-request
    //   con email cliente esistente -> success=true + defaultPassword="Cliente123!"
    // --------------------------------------------------------
    @Test
    void tc3_01_resetRequest_clienteEsistente_restuisceSuccessTrueEDefaultPassword() throws Exception {
        Cliente c = new Cliente();
        c.setUsername("chiara.verdi@example.com");
        c.setPassword(PasswordHasher.sha512("VecchiaPassword1!"));
        c.setRuolo(Ruolo.CLIENTE);
        c.setAttivo(true);
        c.setNome("Chiara");
        c.setCognome("Verdi");
        c.setEmail("chiara.verdi@example.com");
        c.setTelefono("3331112222");
        c.setIndirizzo("Via Vecchia 1");
        c.setCodiceFiscale("VRDCHR90A01H501U");
        c.setDataNascita(LocalDate.of(1990, 1, 1));
        clienteRepository.save(c);

        String json = """
            {
              "email": "chiara.verdi@example.com"
            }
            """;

        mockMvc.perform(post("/auth/password/reset-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.defaultPassword").value("Cliente123!"));

        // Verifica che nel DB la password sia cambiata e l'utente sia non attivo
        Cliente updated = clienteRepository.findById(c.getId()).orElseThrow();
        assertEquals(PasswordHasher.sha512("Cliente123!"), updated.getPassword());
        org.junit.jupiter.api.Assertions.assertFalse(updated.isAttivo());
    }

    // --------------------------------------------------------
    // TC3_02 (controller) - /auth/password/reset-request
    //   con email inesistente -> success=false + defaultPassword=null
    // --------------------------------------------------------
    @Test
    void tc3_02_resetRequest_emailNonEsistente_restuisceSuccessFalse() throws Exception {
        String json = """
            {
              "email": "sconosciuto@example.com"
            }
            """;

        mockMvc.perform(post("/auth/password/reset-request")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.defaultPassword", is(nullValue())));
    }

    // --------------------------------------------------------
    // TC3_03 (controller) - /auth/password/first-change
    //   flusso completo: login con default, poi cambio password
    // --------------------------------------------------------
    @Test
    void tc3_03_firstChange_flussoCompleto_restuisceTrueEPasswordAggiornata() throws Exception {
        // Creo utente NON attivo con password di default Cliente123!
        Cliente c = new Cliente();
        c.setUsername("chiara.verdi@example.com");
        c.setPassword(PasswordHasher.sha512("Cliente123!"));
        c.setRuolo(Ruolo.CLIENTE);
        c.setAttivo(false);
        c.setNome("Chiara");
        c.setCognome("Verdi");
        c.setEmail("chiara.verdi@example.com");
        clienteRepository.save(c);

        // 1) login per ottenere token
        String loginJson = """
            {
              "email": "chiara.verdi@example.com",
              "password": "Cliente123!"
            }
            """;

        String loginResponse = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.mustChangePassword").value(true))
                .andReturn()
                .getResponse()
                .getContentAsString();

        // estraggo il token alla buona
        String token = loginResponse.replaceAll(".*\"token\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        // 2) chiamata a /auth/password/first-change
        String changeJson = """
            {
              "token": "Bearer %s",
              "nuovaPassword": "NuovaPassword123!"
            }
            """.formatted(token);

        mockMvc.perform(post("/auth/password/first-change")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(changeJson))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));

        // Verifica sul DB
        Cliente updated = clienteRepository.findById(c.getId()).orElseThrow();
        assertEquals(PasswordHasher.sha512("NuovaPassword123!"), updated.getPassword());
        org.junit.jupiter.api.Assertions.assertTrue(updated.isAttivo());
    }
}