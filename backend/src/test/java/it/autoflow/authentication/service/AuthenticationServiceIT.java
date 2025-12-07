package it.autoflow.authentication.service;

import it.autoflow.authentication.dto.LoginRequestDTO;
import it.autoflow.authentication.dto.LoginResponseDTO;
import it.autoflow.authentication.utils.PasswordHasher;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.entity.Ruolo;
import it.autoflow.user.repository.ClienteRepository;
import it.autoflow.user.repository.UserRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di integrazione per TC1 (Login / Logout) sul SERVICE.
 * Usa il contesto completo Spring Boot + DB H2 (profilo "test").
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthenticationServiceIT {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // -------------------------------------------------------
    // TC1_01 - Request null -> IllegalArgumentException
    // -------------------------------------------------------
    @Test
    void tc1_01_loginRequestNull_lanciaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.login(null)
        );
        assertEquals("Credenziali non valide", ex.getMessage());
    }

    // -------------------------------------------------------
    // TC1_02 - Email null -> IllegalArgumentException
    // -------------------------------------------------------
    @Test
    void tc1_02_loginEmailNull_lanciaIllegalArgumentException() {
        LoginRequestDTO req = new LoginRequestDTO();
        req.setEmail(null);
        req.setPassword("qualcosa");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.login(req)
        );
        assertEquals("Credenziali non valide", ex.getMessage());
    }

    // -------------------------------------------------------
    // TC1_03 - Password null -> IllegalArgumentException
    // -------------------------------------------------------
    @Test
    void tc1_03_loginPasswordNull_lanciaIllegalArgumentException() {
        LoginRequestDTO req = new LoginRequestDTO();
        req.setEmail("chiara.verdi@example.com");
        req.setPassword(null);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.login(req)
        );
        assertEquals("Credenziali non valide", ex.getMessage());
    }

    // -------------------------------------------------------
    // TC1_04 - Utente non esistente -> IllegalArgumentException("Utente non trovato")
    // -------------------------------------------------------
    @Test
    void tc1_04_loginUtenteNonEsistente_lanciaIllegalArgumentException() {
        LoginRequestDTO req = new LoginRequestDTO();
        req.setEmail("inesistente@example.com");
        req.setPassword("Qualcosa123!");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.login(req)
        );
        assertEquals("Utente non trovato", ex.getMessage());
    }

    // -------------------------------------------------------
    // TC1_05 - Password errata -> IllegalArgumentException("Password errata")
    // -------------------------------------------------------
    @Test
    void tc1_05_loginPasswordErrata_lanciaIllegalArgumentException() {
        // arrange
        Cliente c = new Cliente();
        c.setUsername("chiara.verdi@example.com");
        c.setPassword(PasswordHasher.sha512("PasswordCorretta1!"));
        c.setRuolo(Ruolo.CLIENTE);
        c.setAttivo(true);
        c.setNome("Chiara");
        c.setCognome("Verdi");
        c.setEmail("chiara.verdi@example.com");
        clienteRepository.save(c);

        LoginRequestDTO req = new LoginRequestDTO();
        req.setEmail("chiara.verdi@example.com");
        req.setPassword("PasswordSbagliata!");

        // act + assert
        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> authenticationService.login(req)
        );
        assertEquals("Password errata", ex.getMessage());
    }

    // -------------------------------------------------------
    // TC1_06 - Account NON attivo + password errata -> IllegalStateException("Account non attivo")
    // -------------------------------------------------------
    @Test
    void tc1_06_loginAccountNonAttivoPasswordErrata_lanciaIllegalStateException() {
        Cliente c = new Cliente();
        c.setUsername("chiara.verdi@example.com");
        c.setPassword(PasswordHasher.sha512("PasswordDefault1!"));
        c.setRuolo(Ruolo.CLIENTE);
        c.setAttivo(false); // account non attivo
        c.setNome("Chiara");
        c.setCognome("Verdi");
        c.setEmail("chiara.verdi@example.com");
        clienteRepository.save(c);

        LoginRequestDTO req = new LoginRequestDTO();
        req.setEmail("chiara.verdi@example.com");
        req.setPassword("PasswordSbagliata!");

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> authenticationService.login(req)
        );
        assertEquals("Account non attivo", ex.getMessage());
    }

    // -------------------------------------------------------
    // TC1_07 - Account NON attivo + password corretta
    //           -> login OK, mustChangePassword = true
    // -------------------------------------------------------
    @Test
    void tc1_07_loginAccountNonAttivoPasswordCorretta_restuisceMustChangeTrueEToken() {
        Cliente c = new Cliente();
        c.setUsername("chiara.verdi@example.com");
        c.setPassword(PasswordHasher.sha512("PasswordDefault1!"));
        c.setRuolo(Ruolo.CLIENTE);
        c.setAttivo(false); // utente in stato "deve cambiare password"
        c.setNome("Chiara");
        c.setCognome("Verdi");
        c.setEmail("chiara.verdi@example.com");
        clienteRepository.save(c);

        LoginRequestDTO req = new LoginRequestDTO();
        req.setEmail("chiara.verdi@example.com");
        req.setPassword("PasswordDefault1!");

        LoginResponseDTO resp = authenticationService.login(req);

        assertNotNull(resp);
        assertNotNull(resp.getToken());
        assertEquals(c.getId(), resp.getUserId());
        assertEquals("CLIENTE", resp.getRuolo());
        assertTrue(resp.isMustChangePassword());
    }

    // -------------------------------------------------------
    // TC1_08 - Login valido, account attivo -> token + mustChangePassword=false
    // -------------------------------------------------------
    @Test
    void tc1_08_loginValidoAccountAttivo_restuisceTokenEMustChangeFalse() {
        Cliente c = new Cliente();
        c.setUsername("chiara.verdi@example.com");
        c.setPassword(PasswordHasher.sha512("AutoFlow2025!"));
        c.setRuolo(Ruolo.CLIENTE);
        c.setAttivo(true);
        c.setNome("Chiara");
        c.setCognome("Verdi");
        c.setEmail("chiara.verdi@example.com");
        clienteRepository.save(c);

        LoginRequestDTO req = new LoginRequestDTO();
        req.setEmail("chiara.verdi@example.com");
        req.setPassword("AutoFlow2025!");

        LoginResponseDTO resp = authenticationService.login(req);

        assertNotNull(resp);
        assertNotNull(resp.getToken());
        assertEquals(c.getId(), resp.getUserId());
        assertEquals("CLIENTE", resp.getRuolo());
        assertFalse(resp.isMustChangePassword());
    }

    // -------------------------------------------------------
    // TC1_09 - Logout con token null/blank -> false
    // -------------------------------------------------------
    @Test
    void tc1_09_logoutTokenNullOBlank_restituisceFalse() {
        assertFalse(authenticationService.logout(null));
        assertFalse(authenticationService.logout(""));
        assertFalse(authenticationService.logout("   "));
    }

    // -------------------------------------------------------
    // TC1_10 - Logout con token valido precedentemente salvato -> true
    // -------------------------------------------------------
    @Test
    void tc1_10_logoutTokenValido_restituisceTrue() {
        // creo un utente valido
        Cliente c = new Cliente();
        c.setUsername("chiara.verdi@example.com");
        c.setPassword(PasswordHasher.sha512("AutoFlow2025!"));
        c.setRuolo(Ruolo.CLIENTE);
        c.setAttivo(true);
        c.setNome("Chiara");
        c.setCognome("Verdi");
        c.setEmail("chiara.verdi@example.com");
        clienteRepository.save(c);

        // login per ottenere il token reale gestito dal service
        LoginRequestDTO req = new LoginRequestDTO();
        req.setEmail("chiara.verdi@example.com");
        req.setPassword("AutoFlow2025!");

        LoginResponseDTO resp = authenticationService.login(req);
        String token = resp.getToken();
        assertNotNull(token);

        // logout
        boolean result = authenticationService.logout(token);
        assertTrue(result);
    }
}