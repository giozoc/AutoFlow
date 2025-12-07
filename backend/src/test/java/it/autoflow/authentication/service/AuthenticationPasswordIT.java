package it.autoflow.authentication.service;

import it.autoflow.authentication.dto.LoginRequestDTO;
import it.autoflow.authentication.dto.LoginResponseDTO;
import it.autoflow.authentication.dto.PasswordFirstChangeDTO;
import it.autoflow.authentication.dto.PasswordResetResultDTO;
import it.autoflow.authentication.utils.PasswordHasher;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.entity.Ruolo;
import it.autoflow.user.repository.ClienteRepository;
import it.autoflow.user.repository.UserRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di integrazione per TC3 (Cambio password cliente) sul SERVICE.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class AuthenticationPasswordIT {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    // -------------------------------------------------------
    // TC3_01 - requestPasswordReset con email null/vuota -> success=false
    // -------------------------------------------------------
    @Test
    void tc3_01_requestPasswordReset_emailNullOvuota_restituisceSuccessFalse() {
        PasswordResetResultDTO r1 = authenticationService.requestPasswordReset(null);
        PasswordResetResultDTO r2 = authenticationService.requestPasswordReset("");

        assertFalse(r1.isSuccess());
        assertNull(r1.getDefaultPassword());

        assertFalse(r2.isSuccess());
        assertNull(r2.getDefaultPassword());
    }

    // -------------------------------------------------------
    // TC3_02 - requestPasswordReset con email inesistente -> success=false
    // -------------------------------------------------------
    @Test
    void tc3_02_requestPasswordReset_emailNonEsistente_restituisceSuccessFalse() {
        PasswordResetResultDTO result =
                authenticationService.requestPasswordReset("unknown@example.com");

        assertFalse(result.isSuccess());
        assertNull(result.getDefaultPassword());
    }

    // -------------------------------------------------------
    // TC3_03 - requestPasswordReset per cliente esistente:
    //          - success=true
    //          - defaultPassword = "Cliente123!"
    //          - user.password aggiornata e attivo=false
    // -------------------------------------------------------
    @Test
    void tc3_03_requestPasswordReset_clienteEsistente_impostaDefaultPasswordEDisattiva() {
        // arrange: creo cliente attivo con una password qualsiasi
        Cliente c = new Cliente();
        c.setUsername("chiara.verdi@example.com");
        c.setPassword(PasswordHasher.sha512("VecchiaPassword1!"));
        c.setRuolo(Ruolo.CLIENTE);
        c.setAttivo(true);
        c.setNome("Chiara");
        c.setCognome("Verdi");
        c.setEmail("chiara.verdi@example.com");
        clienteRepository.save(c);

        // act
        PasswordResetResultDTO result =
                authenticationService.requestPasswordReset("chiara.verdi@example.com");

        // assert DTO
        assertTrue(result.isSuccess());
        assertEquals("Cliente123!", result.getDefaultPassword());

        // assert sul DB
        Optional<Cliente> opt = clienteRepository.findById(c.getId());
        assertTrue(opt.isPresent());
        Cliente updated = opt.get();

        assertFalse(updated.isAttivo());
        assertEquals(
                PasswordHasher.sha512("Cliente123!"),
                updated.getPassword()
        );
    }

    // -------------------------------------------------------
    // TC3_04 - changePasswordAfterReset con token valido
    //          e account non attivo -> password cambiata, attivo=true
    // -------------------------------------------------------
    @Test
    void tc3_04_changePasswordAfterReset_conTokenValidoEAccountNonAttivo_cambiaPasswordEAttiva() {
        // arrange: utente non attivo con password di default
        Cliente c = new Cliente();
        c.setUsername("chiara.verdi@example.com");
        c.setPassword(PasswordHasher.sha512("Cliente123!"));
        c.setRuolo(Ruolo.CLIENTE);
        c.setAttivo(false);
        c.setNome("Chiara");
        c.setCognome("Verdi");
        c.setEmail("chiara.verdi@example.com");
        clienteRepository.save(c);

        // faccio login con la password di default per ottenere il token
        LoginRequestDTO loginReq = new LoginRequestDTO();
        loginReq.setEmail("chiara.verdi@example.com");
        loginReq.setPassword("Cliente123!");

        LoginResponseDTO loginResp = authenticationService.login(loginReq);
        String token = loginResp.getToken();
        assertNotNull(token);
        assertTrue(loginResp.isMustChangePassword());

        // preparo DTO per changePasswordAfterReset
        PasswordFirstChangeDTO dto = new PasswordFirstChangeDTO();
        dto.setToken("Bearer " + token);
        dto.setNuovaPassword("NuovaPassword123!");

        // act
        boolean result = authenticationService.changePasswordAfterReset(dto);

        // assert
        assertTrue(result);

        Cliente updated = clienteRepository.findById(c.getId()).orElseThrow();
        assertTrue(updated.isAttivo());
        assertEquals(
                PasswordHasher.sha512("NuovaPassword123!"),
                updated.getPassword()
        );
    }

    // -------------------------------------------------------
    // TC3_05 - changePasswordAfterReset con token inesistente -> false
    // -------------------------------------------------------
    @Test
    void tc3_05_changePasswordAfterReset_tokenInesistente_restituisceFalse() {
        PasswordFirstChangeDTO dto = new PasswordFirstChangeDTO();
        dto.setToken("Bearer token-inventato");
        dto.setNuovaPassword("Qualcosa123!");

        boolean result = authenticationService.changePasswordAfterReset(dto);

        assertFalse(result);
    }

    // -------------------------------------------------------
    // TC3_06 - changePasswordAfterReset su utente già attivo
    //          -> false e password invariata
    // -------------------------------------------------------
    @Test
    void tc3_06_changePasswordAfterReset_utenteGiaAttivo_restituisceFalseESenzaModifiche() {
        // utente già attivo con password corrente
        Cliente c = new Cliente();
        c.setUsername("mario.rossi@example.com");
        c.setPassword(PasswordHasher.sha512("MarioCorrente123!"));
        c.setRuolo(Ruolo.CLIENTE);
        c.setAttivo(true);
        c.setNome("Mario");
        c.setCognome("Rossi");
        c.setEmail("mario.rossi@example.com");
        clienteRepository.save(c);

        // login normale
        LoginRequestDTO loginReq = new LoginRequestDTO();
        loginReq.setEmail("mario.rossi@example.com");
        loginReq.setPassword("MarioCorrente123!");

        LoginResponseDTO loginResp = authenticationService.login(loginReq);
        String token = loginResp.getToken();
        assertNotNull(token);
        assertFalse(loginResp.isMustChangePassword());

        // tentativo di cambio password "post reset"
        PasswordFirstChangeDTO dto = new PasswordFirstChangeDTO();
        dto.setToken("Bearer " + token);
        dto.setNuovaPassword("NuovaPassword999!");

        boolean result = authenticationService.changePasswordAfterReset(dto);

        assertFalse(result);

        Cliente updated = clienteRepository.findById(c.getId()).orElseThrow();
        // password deve essere invariata
        assertEquals(
                PasswordHasher.sha512("MarioCorrente123!"),
                updated.getPassword()
        );
        assertTrue(updated.isAttivo());
    }
}