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
import org.springframework.test.context.TestPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Test di integrazione per TC1 sul CONTROLLER /auth.
 * Usa MockMvc + contesto completo Spring (service + repository reali).
 */
@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
@Transactional
class AuthenticationControllerIT {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ClienteRepository clienteRepository;

    // --------------------------------------------
    // TC1_11 - /auth/login con credenziali corrette
    //          -> 200 + JSON con userId, ruolo, mustChangePassword=false
    // --------------------------------------------
    @Test
    void tc1_11_loginEndpointValido_restuisce200EJsonCorretto() throws Exception {
        Cliente c = new Cliente();
        c.setUsername("chiara.verdi@example.com");
        c.setPassword(PasswordHasher.sha512("AutoFlow2025!"));
        c.setRuolo(Ruolo.CLIENTE);
        c.setAttivo(true);
        c.setNome("Chiara");
        c.setCognome("Verdi");
        c.setEmail("chiara.verdi@example.com");
        clienteRepository.save(c);

        String json = """
                {
                  "email": "chiara.verdi@example.com",
                  "password": "AutoFlow2025!"
                }
                """;

        mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(json))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.userId").value(c.getId()))
                .andExpect(jsonPath("$.ruolo").value("CLIENTE"))
                .andExpect(jsonPath("$.mustChangePassword").value(false))
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    // --------------------------------------------
    // TC1_12 - /auth/logout con token valido -> 200 + "true"
    // --------------------------------------------
    @Test
    void tc1_12_logoutEndpointTokenValido_restuisce200ETrue() throws Exception {
        // creo utente e faccio login tramite service? NO:
        // qui simuliamo semplicemente il token che il service si aspetta.
        // ma per coerenza è meglio usare davvero il login del backend.
        Cliente c = new Cliente();
        c.setUsername("chiara.verdi@example.com");
        c.setPassword(PasswordHasher.sha512("AutoFlow2025!"));
        c.setRuolo(Ruolo.CLIENTE);
        c.setAttivo(true);
        c.setNome("Chiara");
        c.setCognome("Verdi");
        c.setEmail("chiara.verdi@example.com");
        clienteRepository.save(c);

        String loginJson = """
                {
                  "email": "chiara.verdi@example.com",
                  "password": "AutoFlow2025!"
                }
                """;

        // facciamo una prima chiamata a /auth/login per ottenere il token
        String token = mockMvc.perform(post("/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(loginJson))
                .andExpect(status().isOk())
                .andReturn()
                .getResponse()
                .getContentAsString();

        // estraiamo il valore del token alla buona (non elegantissimo ma funziona per i test)
        // es: {"token":"xxx","ruolo":"CLIENTE",...}
        String rawToken = token.replaceAll(".*\"token\"\\s*:\\s*\"([^\"]+)\".*", "$1");

        mockMvc.perform(post("/auth/logout")
                        .header("Authorization", rawToken))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }
}