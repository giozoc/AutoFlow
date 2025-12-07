package it.autoflow.user.service;

import it.autoflow.user.entity.Cliente;
import it.autoflow.user.repository.ClienteRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
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
class ClienteServiceImplTest {

    @Mock
    private ClienteRepository clienteRepository;

    @InjectMocks
    private ClienteServiceImpl clienteService;

    private Cliente existingAttivo;
    private Cliente existingDisattivo;

    @BeforeEach
    void setup() {
        existingAttivo = new Cliente();
        existingAttivo.setId(1L);
        existingAttivo.setNome("Mario");
        existingAttivo.setCognome("Rossi");
        existingAttivo.setEmail("mario.rossi@example.com");
        existingAttivo.setTelefono("3491122334");
        existingAttivo.setIndirizzo("Via Vecchia 1");
        existingAttivo.setAttivo(true);
        existingAttivo.setCodiceFiscale("RSSMRA80A01H501U");
        existingAttivo.setDataNascita(LocalDate.of(1980, 1, 1));
        existingAttivo.setUsername("mario.rossi@example.com");
        existingAttivo.setPassword("HASH_OLD");

        existingDisattivo = new Cliente();
        existingDisattivo.setId(2L);
        existingDisattivo.setNome("Mario");
        existingDisattivo.setCognome("Rossi");
        existingDisattivo.setEmail("mario.rossi@example.com");
        existingDisattivo.setTelefono("3491122334");
        existingDisattivo.setIndirizzo("Via Vecchia 1");
        existingDisattivo.setAttivo(false);
        existingDisattivo.setCodiceFiscale("RSSMRA80A01H501U");
        existingDisattivo.setDataNascita(LocalDate.of(1980, 1, 1));
        existingDisattivo.setUsername("mario.rossi@example.com");
        existingDisattivo.setPassword("HASH_OLD");
    }

    // TC2_01 – Profilo aggiornato correttamente
    @Test
    void tc2_01_profiloValido_aggiornaCorrettamente() {
        Long id = 1L;

        Cliente input = new Cliente();
        input.setNome("Chiara");
        input.setCognome("Verdi");
        input.setEmail("chiara.verdi@example.com");
        input.setTelefono("3491122334");
        input.setIndirizzo("Via Nuova 10");
        input.setAttivo(true);
        input.setCodiceFiscale("VRDCHR90B02H501U");
        input.setDataNascita(LocalDate.of(1990, 2, 2));

        when(clienteRepository.findById(id)).thenReturn(Optional.of(existingAttivo));
        when(clienteRepository.save(any(Cliente.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cliente result = clienteService.update(id, input);

        assertNotNull(result);
        assertEquals("Chiara", result.getNome());
        assertEquals("Verdi", result.getCognome());
        assertEquals("chiara.verdi@example.com", result.getEmail());
        assertEquals("3491122334", result.getTelefono());
        assertEquals("Via Nuova 10", result.getIndirizzo());
        assertTrue(result.isAttivo());
        assertEquals("VRDCHR90B02H501U", result.getCodiceFiscale());
        assertEquals(LocalDate.of(1990, 2, 2), result.getDataNascita());
        assertEquals("chiara.verdi@example.com", result.getUsername());
    }

    // TC2_02 – Nome troppo corto (<2)
    @Test
    void tc2_02_nomeTroppoCorto_lanciaEccezioneConMessaggioCorretto() {
        Long id = 1L;
        Cliente input = new Cliente();
        input.setNome("C"); // <2
        input.setCognome("Verdi");
        input.setTelefono("3491122334");

        when(clienteRepository.findById(id)).thenReturn(Optional.of(existingAttivo));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> clienteService.update(id, input));

        assertEquals("Nome troppo corto", ex.getMessage());
    }

    // TC2_03 – Nome troppo lungo (>30)
    @Test
    void tc2_03_nomeTroppoLungo_lanciaEccezioneConMessaggioCorretto() {
        Long id = 1L;
        Cliente input = new Cliente();
        input.setNome("ChiaraConNomeEstremamenteLungoDiTest"); // >30
        input.setCognome("Verdi");
        input.setTelefono("3491122334");

        when(clienteRepository.findById(id)).thenReturn(Optional.of(existingAttivo));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> clienteService.update(id, input));

        assertEquals("Nome troppo lungo", ex.getMessage());
    }

    // TC2_04 – Nome con caratteri non ammessi
    @Test
    void tc2_04_nomeConCaratteriNonAmmessi_lanciaFormatoNomeNonValido() {
        Long id = 1L;
        Cliente input = new Cliente();
        input.setNome("Chiara3");
        input.setCognome("Verdi");
        input.setTelefono("3491122334");

        when(clienteRepository.findById(id)).thenReturn(Optional.of(existingAttivo));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> clienteService.update(id, input));

        assertEquals("Formato nome non valido", ex.getMessage());
    }

    // TC2_05 – Cognome troppo corto (<2)
    @Test
    void tc2_05_cognomeTroppoCorto_lanciaEccezioneConMessaggioCorretto() {
        Long id = 1L;
        Cliente input = new Cliente();
        input.setNome("Chiara");
        input.setCognome("V"); // <2
        input.setTelefono("3491122334");

        when(clienteRepository.findById(id)).thenReturn(Optional.of(existingAttivo));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> clienteService.update(id, input));

        assertEquals("Cognome troppo corto", ex.getMessage());
    }

    // TC2_06 – Cognome troppo lungo (>40)
    @Test
    void tc2_06_cognomeTroppoLungo_lanciaEccezioneConMessaggioCorretto() {
        Long id = 1L;
        Cliente input = new Cliente();
        input.setNome("Chiara");
        input.setCognome("VerdiConCognomeDavveroMoltoMoltoLungoTest"); // >40
        input.setTelefono("3491122334");

        when(clienteRepository.findById(id)).thenReturn(Optional.of(existingAttivo));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> clienteService.update(id, input));

        assertEquals("Cognome troppo lungo", ex.getMessage());
    }

    // TC2_07 – Cognome con caratteri non ammessi
    @Test
    void tc2_07_cognomeConCaratteriNonAmmessi_lanciaFormatoCognomeNonValido() {
        Long id = 1L;
        Cliente input = new Cliente();
        input.setNome("Chiara");
        input.setCognome("Verdi7");
        input.setTelefono("3491122334");

        when(clienteRepository.findById(id)).thenReturn(Optional.of(existingAttivo));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> clienteService.update(id, input));

        assertEquals("Formato cognome non valido", ex.getMessage());
    }

    // TC2_08 – Numero di telefono con lunghezza < 10
    @Test
    void tc2_08_telefonoTroppoCorto_lanciaNumeroTelefonoNonValido() {
        Long id = 1L;
        Cliente input = new Cliente();
        input.setNome("Chiara");
        input.setCognome("Verdi");
        input.setTelefono("349112233"); // 9 cifre

        when(clienteRepository.findById(id)).thenReturn(Optional.of(existingAttivo));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> clienteService.update(id, input));

        assertEquals("Numero di telefono non valido", ex.getMessage());
    }

    // TC2_09 – Numero di telefono con caratteri non numerici
    @Test
    void tc2_09_telefonoConCaratteriNonNumerici_lanciaFormatoTelefonoNonValido() {
        Long id = 1L;
        Cliente input = new Cliente();
        input.setNome("Chiara");
        input.setCognome("Verdi");
        input.setTelefono("34911A2334"); // contiene A

        when(clienteRepository.findById(id)).thenReturn(Optional.of(existingAttivo));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> clienteService.update(id, input));

        assertEquals("Formato telefono non valido", ex.getMessage());
    }

    // TC2_10 – Account disattivato
    @Test
    void tc2_10_accountDisattivato_lanciaIllegalStateException() {
        Long id = 2L;
        Cliente input = new Cliente();
        input.setNome("Chiara");
        input.setCognome("Verdi");
        input.setTelefono("3491122334");

        when(clienteRepository.findById(id)).thenReturn(Optional.of(existingDisattivo));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> clienteService.update(id, input));

        assertEquals("Account disattivato", ex.getMessage());
    }
}