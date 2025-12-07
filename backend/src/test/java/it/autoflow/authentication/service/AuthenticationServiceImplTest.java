package it.autoflow.authentication.service;

import it.autoflow.authentication.dto.*;
import it.autoflow.authentication.utils.PasswordHasher;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.entity.Ruolo;
import it.autoflow.user.entity.User;
import it.autoflow.user.repository.ClienteRepository;
import it.autoflow.user.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class AuthenticationServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private AuthenticationServiceImpl authenticationService;

    private User userAttivo;
    private User userDisattivo;

    @BeforeEach
    void setup() {
        // Cliente ATTIVO
        Cliente clienteAttivo = new Cliente();
        clienteAttivo.setId(1L);
        clienteAttivo.setUsername("chiara.verdi@example.com");
        clienteAttivo.setRuolo(Ruolo.CLIENTE);
        clienteAttivo.setAttivo(true);
        clienteAttivo.setPassword(PasswordHasher.sha512("AutoFlow2025!"));

        // Cliente DISATTIVO
        Cliente clienteDisattivo = new Cliente();
        clienteDisattivo.setId(2L);
        clienteDisattivo.setUsername("chiara.verdi@example.com");
        clienteDisattivo.setRuolo(Ruolo.CLIENTE);
        clienteDisattivo.setAttivo(false);
        clienteDisattivo.setPassword(PasswordHasher.sha512("AutoFlow2025!"));

        userAttivo = clienteAttivo;
        userDisattivo = clienteDisattivo;
    }

    // TC1_01 – login ok (utente attivo)
    @Test
    void tc1_01_loginUtenteAttivoCredenzialiCorrette_restuisceTokenEMustChangePasswordFalse() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("chiara.verdi@example.com");
        request.setPassword("AutoFlow2025!");

        when(userRepository.findByUsername("chiara.verdi@example.com"))
                .thenReturn(Optional.of(userAttivo));

        LoginResponseDTO response = authenticationService.login(request);

        assertNotNull(response);
        assertNotNull(response.getToken());
        assertFalse(response.getToken().isBlank());
        assertEquals(userAttivo.getId(), response.getUserId());
        assertEquals(Ruolo.CLIENTE.name(), response.getRuolo());
        assertFalse(response.isMustChangePassword());
    }

    // TC1_02 – login utente disattivo, password corretta (primo accesso)
    @Test
    void tc1_02_loginUtenteDisattivoPasswordCorretta_mustChangePasswordTrue() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("chiara.verdi@example.com");
        request.setPassword("AutoFlow2025!");

        when(userRepository.findByUsername("chiara.verdi@example.com"))
                .thenReturn(Optional.of(userDisattivo));

        LoginResponseDTO response = authenticationService.login(request);

        assertNotNull(response);
        assertTrue(response.isMustChangePassword());
        assertEquals(userDisattivo.getId(), response.getUserId());
    }

    // TC1_03 – utente disattivo + password sbagliata → IllegalStateException
    @Test
    void tc1_03_loginUtenteDisattivoPasswordErrata_lanciaIllegalStateException() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("chiara.verdi@example.com");
        request.setPassword("PasswordSbagliata!");

        when(userRepository.findByUsername("chiara.verdi@example.com"))
                .thenReturn(Optional.of(userDisattivo));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> authenticationService.login(request));

        assertEquals("Account non attivo", ex.getMessage());
    }

    // TC1_04 – utente non trovato
    @Test
    void tc1_04_loginUtenteInesistente_lanciaIllegalArgumentException() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("sconosciuto@example.com");
        request.setPassword("AutoFlow2025!");

        when(userRepository.findByUsername("sconosciuto@example.com"))
                .thenReturn(Optional.empty());

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authenticationService.login(request));

        assertEquals("Utente non trovato", ex.getMessage());
    }

    // TC1_05 – password errata (utente attivo)
    @Test
    void tc1_05_loginPasswordErrata_lanciaIllegalArgumentException() {
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("chiara.verdi@example.com");
        request.setPassword("PasswordErrata!");

        when(userRepository.findByUsername("chiara.verdi@example.com"))
                .thenReturn(Optional.of(userAttivo));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authenticationService.login(request));

        assertEquals("Password errata", ex.getMessage());
    }

    // TC1_06 – request null
    @Test
    void tc1_06_loginRequestNull_lanciaIllegalArgumentException() {
        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> authenticationService.login(null));

        assertEquals("Credenziali non valide", ex.getMessage());
    }

    // TC1_07 – logout con token valido
    @Test
    void tc1_07_logoutConTokenValido_restuisceTrueERimuoveToken() {
        // primo login
        LoginRequestDTO request = new LoginRequestDTO();
        request.setEmail("chiara.verdi@example.com");
        request.setPassword("AutoFlow2025!");

        when(userRepository.findByUsername("chiara.verdi@example.com"))
                .thenReturn(Optional.of(userAttivo));

        LoginResponseDTO response = authenticationService.login(request);
        String token = response.getToken();

        assertTrue(authenticationService.logout(token));
        // ripetere il logout sullo stesso token → false
        assertFalse(authenticationService.logout(token));
    }

    // TC1_08 – logout con token nullo/vuoto
    @Test
    void tc1_08_logoutConTokenNullOVuoto_restuisceFalse() {
        assertFalse(authenticationService.logout(null));
        assertFalse(authenticationService.logout(""));
        assertFalse(authenticationService.logout("   "));
    }

    // Esempio per registerCliente (ci servirà più avanti per i TC di registrazione)
    @Test
    void registerCliente_conDatiValidi_salvaClienteSuRepository() {
        RegisterClienteDTO dto = new RegisterClienteDTO();
        dto.setEmail("nuovo.cliente@example.com");
        dto.setPassword("Password123!");
        dto.setNome("Mario");
        dto.setCognome("Rossi");
        dto.setTelefono("3331234567");
        dto.setIndirizzo("Via Roma 1");
        dto.setCodiceFiscale("RSSMRA80A01H501U");
        dto.setDataNascita(LocalDate.of(1980, 1, 1));

        when(userRepository.findByUsername("nuovo.cliente@example.com"))
                .thenReturn(Optional.empty());
        when(clienteRepository.save(any(Cliente.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        authenticationService.registerCliente(dto);

        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void registerCliente_emailGiaUsata_lanciaIllegalArgumentException() {
        RegisterClienteDTO dto = new RegisterClienteDTO();
        dto.setEmail("chiara.verdi@example.com");
        dto.setPassword("Password123!");

        when(userRepository.findByUsername("chiara.verdi@example.com"))
                .thenReturn(Optional.of(userAttivo));

        assertThrows(IllegalArgumentException.class,
                () -> authenticationService.registerCliente(dto));
    }

    // ============================================================
    // TC1_11 e TC1_12 - validazione formale password (frontend)
    // ============================================================

    /**
     * TC1_11 – Password non conforme (mancano alcuni tipi di caratteri).
     *
     * Nel TCS questo caso prevede il messaggio:
     * "Password non conforme."
     *
     * Questa validazione viene effettuata a livello di frontend (TypeScript/React)
     * prima di inviare la richiesta al backend, quindi non è gestita da
     * AuthenticationServiceImpl. Per questo motivo il test viene marcato come
     * @Disabled nel backend e coperto nei test di sistema (Selenium).
     */
    @org.junit.jupiter.api.Disabled("TC1_11: validazione password gestita lato frontend e testata con Selenium, non dal service backend.")
    @Test
    void tc1_11_passwordNonConforme_gestitaDalFrontend() {
        // la logica è sul frontend; qui non c'è comportamento specifico da verificare
    }

    /**
     * TC1_12 – Password troppo corta (lunghezza < 8 caratteri).
     *
     * Nel TCS questo caso prevede il messaggio:
     * "Minimo 8 caratteri."
     *
     * Anche questa validazione è prevista lato frontend; il backend riceve richieste
     * solo dopo il controllo di lunghezza. Di conseguenza, nel backend non c'è
     * una logica distinta per questo caso: viene coperto dai test di sistema.
     */
    @org.junit.jupiter.api.Disabled("TC1_12: controllo lunghezza minima password gestito lato frontend e testato con Selenium.")
    @Test
    void tc1_12_passwordTroppoCorta_gestitaDalFrontend() {
        // idem come sopra: nessun comportamento specifico a livello di service
    }

    //TC3

    @Test
    void tc3_01_requestPasswordReset_emailNullORVuota_restituisceSuccessFalse() {
        PasswordResetResultDTO result1 = authenticationService.requestPasswordReset(null);
        PasswordResetResultDTO result2 = authenticationService.requestPasswordReset("");

        assertFalse(result1.isSuccess());
        assertNull(result1.getDefaultPassword());

        assertFalse(result2.isSuccess());
        assertNull(result2.getDefaultPassword());

        verify(userRepository, never()).save(any());
    }
    @Test
    void tc3_02_requestPasswordReset_emailNonEsistente_restituisceSuccessFalse() {
        String email = "unknown@example.com";

        when(userRepository.findByUsername(email)).thenReturn(Optional.empty());

        PasswordResetResultDTO result = authenticationService.requestPasswordReset(email);

        assertFalse(result.isSuccess());
        assertNull(result.getDefaultPassword());
        verify(userRepository, never()).save(any());
    }

    @Test
    void tc3_03_requestPasswordReset_clienteEsistente_impostaDefaultPasswordEDisattiva() {
        String email = "chiara.verdi@example.com";

        User user = new Cliente(); // va bene anche new User() se non astratta, ma Cliente è ok
        user.setId(1L);
        user.setUsername(email);
        user.setPassword(PasswordHasher.sha512("VecchiaPassword!"));
        user.setRuolo(Ruolo.CLIENTE);
        user.setAttivo(true);

        when(userRepository.findByUsername(email)).thenReturn(Optional.of(user));

        ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
        PasswordResetResultDTO result = authenticationService.requestPasswordReset(email);

        // verifica DTO
        assertTrue(result.isSuccess());
        assertEquals("Cliente123!", result.getDefaultPassword());

        // verifica aggiornamento utente salvato
        verify(userRepository).save(userCaptor.capture());
        User saved = userCaptor.getValue();

        assertFalse(saved.isAttivo());
        assertEquals(PasswordHasher.sha512("Cliente123!"), saved.getPassword());
    }

    @Test
    void tc3_04_changePasswordAfterReset_conTokenValidoEAccountNonAttivo_cambiaPasswordEAttivaAccount() {
        String email = "chiara.verdi@example.com";
        String defaultPassword = "Cliente123!";

        // utente NON attivo, con password di default già impostata
        User user = new Cliente();
        user.setId(10L);
        user.setUsername(email);
        user.setPassword(PasswordHasher.sha512(defaultPassword));
        user.setRuolo(Ruolo.CLIENTE);
        user.setAttivo(false);

        when(userRepository.findByUsername(email)).thenReturn(Optional.of(user));
        when(userRepository.findById(10L)).thenReturn(Optional.of(user));

        // 1) login con password di default -> genera token in sessionTokens
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail(email);
        loginRequest.setPassword(defaultPassword);

        var loginResponse = authenticationService.login(loginRequest);
        String token = loginResponse.getToken(); // token salvato in sessionTokens

        // 2) cambio password al primo accesso
        PasswordFirstChangeDTO dto = new PasswordFirstChangeDTO();
        dto.setToken("Bearer " + token);              // verrà ripulito dal "Bearer "
        dto.setNuovaPassword("NuovaPassword123!");

        boolean result = authenticationService.changePasswordAfterReset(dto);

        assertTrue(result);
        assertTrue(user.isAttivo());
        assertEquals(PasswordHasher.sha512("NuovaPassword123!"), user.getPassword());

        verify(userRepository, atLeastOnce()).save(user);
    }

    @Test
    void tc3_05_changePasswordAfterReset_tokenInesistente_restituisceFalse() {
        PasswordFirstChangeDTO dto = new PasswordFirstChangeDTO();
        dto.setToken("Bearer token-falso");
        dto.setNuovaPassword("Qualsiasi123!");

        boolean result = authenticationService.changePasswordAfterReset(dto);

        assertFalse(result);
        verify(userRepository, never()).findById(any());
        verify(userRepository, never()).save(any());
    }

    @Test
    void tc3_06_changePasswordAfterReset_utenteGiaAttivo_restituisceFalseESenzaModifiche() {
        String email = "mario.rossi@example.com";
        String passwordCorrente = "PasswordAttuale123!";

        User user = new Cliente();
        user.setId(20L);
        user.setUsername(email);
        user.setPassword(PasswordHasher.sha512(passwordCorrente));
        user.setRuolo(Ruolo.CLIENTE);
        user.setAttivo(true);   // ⚠️ già attivo

        when(userRepository.findByUsername(email)).thenReturn(Optional.of(user));
        when(userRepository.findById(20L)).thenReturn(Optional.of(user));

        // faccio login per generare un token valido in sessionTokens
        LoginRequestDTO loginRequest = new LoginRequestDTO();
        loginRequest.setEmail(email);
        loginRequest.setPassword(passwordCorrente);
        var loginResponse = authenticationService.login(loginRequest);
        String token = loginResponse.getToken();

        PasswordFirstChangeDTO dto = new PasswordFirstChangeDTO();
        dto.setToken("Bearer " + token);
        dto.setNuovaPassword("NuovaPassword123!");

        boolean result = authenticationService.changePasswordAfterReset(dto);

        assertFalse(result);
        // password NON deve cambiare
        assertEquals(PasswordHasher.sha512(passwordCorrente), user.getPassword());

        // potrebbe esserci una save fatta da login, quindi facciamo solo ensure che
        // non c'è una seconda modifica "corretta": qui ci basta controllare lo stato finale dell'oggetto
    }
}