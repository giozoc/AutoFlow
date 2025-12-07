package it.autoflow.proposal.service;

import it.autoflow.configuration.entity.Configurazione;
import it.autoflow.configuration.repository.ConfigurazioneRepository;
import it.autoflow.proposal.dto.PropostaDTO;
import it.autoflow.proposal.entity.Proposta;
import it.autoflow.proposal.entity.StatoProposta;
import it.autoflow.proposal.repository.PropostaRepository;
import it.autoflow.user.entity.AddettoVendite;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.repository.AddettoVenditeRepository;
import it.autoflow.user.repository.ClienteRepository;
import it.autoflow.vehicle.entity.StatoVeicolo;
import it.autoflow.vehicle.entity.Veicolo;
import jakarta.persistence.EntityNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@ActiveProfiles("test")
class PropostaServiceImplTest {

    @Mock
    private PropostaRepository propostaRepository;

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private AddettoVenditeRepository addettoVenditeRepository;

    @Mock
    private ConfigurazioneRepository configurazioneRepository;

    @InjectMocks
    private PropostaServiceImpl propostaService;

    private Cliente cliente;
    private AddettoVendite addetto;
    private Configurazione configurazioneValida;
    private Veicolo veicoloDisponibile;

    @BeforeEach
    void setup() {
        cliente = new Cliente();
        cliente.setId(1L);

        addetto = new AddettoVendite();
        addetto.setId(2L);
        addetto.setAttivo(true); // operatore attivo

        veicoloDisponibile = new Veicolo();
        veicoloDisponibile.setId(10L);
        veicoloDisponibile.setStato(StatoVeicolo.DISPONIBILE);
        veicoloDisponibile.setVisibileAlPubblico(true);

        configurazioneValida = new Configurazione();
        configurazioneValida.setId(3L);
        configurazioneValida.setCliente(cliente);
        configurazioneValida.setVeicolo(veicoloDisponibile);
    }

    // TC4_01 – Proposta creata correttamente
    @Test
    void tc4_01_propostaCreataCorrettamente() {
        PropostaDTO dto = new PropostaDTO();
        dto.setClienteId(1L);
        dto.setAddettoVenditeId(2L);
        dto.setConfigurazioneId(3L);
        dto.setPrezzoProposta(25000.0);
        dto.setStato(StatoProposta.BOZZA);
        LocalDateTime creazione = LocalDateTime.of(2025, 1, 10, 10, 0);
        LocalDateTime scadenza = LocalDateTime.of(2025, 2, 10, 10, 0);
        dto.setDataCreazione(creazione);
        dto.setDataScadenza(scadenza);
        dto.setNoteCliente("Note cliente");
        dto.setNoteInterne("Note interne");

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));
        when(addettoVenditeRepository.findById(2L)).thenReturn(Optional.of(addetto));
        when(configurazioneRepository.findById(3L)).thenReturn(Optional.of(configurazioneValida));

        when(propostaRepository.save(any(Proposta.class)))
                .thenAnswer(invocation -> {
                    Proposta p = invocation.getArgument(0);
                    p.setId(100L);
                    return p;
                });

        PropostaDTO result = propostaService.create(dto);

        assertNotNull(result);
        assertEquals(100L, result.getId());
        assertEquals(1L, result.getClienteId());
        assertEquals(2L, result.getAddettoVenditeId());
        assertEquals(3L, result.getConfigurazioneId());
        assertEquals(25000.0, result.getPrezzoProposta());
        assertEquals(StatoProposta.BOZZA, result.getStato());
        assertEquals(creazione, result.getDataCreazione());
        assertEquals(scadenza, result.getDataScadenza());
        assertEquals("Note cliente", result.getNoteCliente());
        assertEquals("Note interne", result.getNoteInterne());

        ArgumentCaptor<Proposta> captor = ArgumentCaptor.forClass(Proposta.class);
        verify(propostaRepository).save(captor.capture());
        Proposta saved = captor.getValue();

        assertEquals(cliente, saved.getCliente());
        assertEquals(addetto, saved.getAddettoVendite());
        assertEquals(configurazioneValida, saved.getConfigurazione());
    }

    // TC4_02 – Cliente non trovato
    @Test
    void tc4_02_clienteNonTrovato_lanciaEntityNotFoundExceptionConMessaggioCorretto() {
        PropostaDTO dto = new PropostaDTO();
        dto.setClienteId(999L);
        dto.setConfigurazioneId(3L);
        dto.setPrezzoProposta(20000.0);
        dto.setStato(StatoProposta.BOZZA);

        when(clienteRepository.findById(999L)).thenReturn(Optional.empty());

        EntityNotFoundException ex = assertThrows(EntityNotFoundException.class,
                () -> propostaService.create(dto));

        assertEquals("Cliente non trovato", ex.getMessage());
        verify(propostaRepository, never()).save(any());
    }

    // TC4_03 – Configurazione non valida
    @Test
    void tc4_03_configurazioneNonValida_lanciaIllegalArgumentException() {
        PropostaDTO dto = new PropostaDTO();
        dto.setClienteId(1L);
        dto.setConfigurazioneId(3L);
        dto.setPrezzoProposta(21000.0);
        dto.setStato(StatoProposta.BOZZA);

        // cliente esiste
        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        // configurazione esiste ma appartiene a un altro cliente
        Cliente altroCliente = new Cliente();
        altroCliente.setId(99L);
        Configurazione configurazioneAltroCliente = new Configurazione();
        configurazioneAltroCliente.setId(3L);
        configurazioneAltroCliente.setCliente(altroCliente);
        configurazioneAltroCliente.setVeicolo(veicoloDisponibile); // veicolo ok

        when(configurazioneRepository.findById(3L)).thenReturn(Optional.of(configurazioneAltroCliente));

        IllegalArgumentException ex = assertThrows(IllegalArgumentException.class,
                () -> propostaService.create(dto));

        assertEquals("Configurazione non valida", ex.getMessage());
        verify(propostaRepository, never()).save(any());
    }

    // TC4_04 – Veicolo non disponibile
    @Test
    void tc4_04_veicoloNonDisponibile_lanciaIllegalStateException() {
        PropostaDTO dto = new PropostaDTO();
        dto.setClienteId(1L);
        dto.setConfigurazioneId(3L);
        dto.setPrezzoProposta(22000.0);
        dto.setStato(StatoProposta.BOZZA);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        Veicolo veicoloVenduto = new Veicolo();
        veicoloVenduto.setId(10L);
        veicoloVenduto.setStato(StatoVeicolo.VENDUTO);          // NON disponibile
        veicoloVenduto.setVisibileAlPubblico(true);

        Configurazione configConVeicoloNonDisponibile = new Configurazione();
        configConVeicoloNonDisponibile.setId(3L);
        configConVeicoloNonDisponibile.setCliente(cliente);
        configConVeicoloNonDisponibile.setVeicolo(veicoloVenduto);

        when(configurazioneRepository.findById(3L)).thenReturn(Optional.of(configConVeicoloNonDisponibile));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> propostaService.create(dto));

        assertEquals("Veicolo non disponibile", ex.getMessage());
        verify(propostaRepository, never()).save(any());
    }

    // TC4_05 – Operatore non autorizzato
    @Test
    void tc4_05_operatoreNonAutorizzato_lanciaIllegalStateException() {
        PropostaDTO dto = new PropostaDTO();
        dto.setClienteId(1L);
        dto.setAddettoVenditeId(2L);  // viene passato un addetto
        dto.setConfigurazioneId(3L);
        dto.setPrezzoProposta(23000.0);
        dto.setStato(StatoProposta.BOZZA);

        when(clienteRepository.findById(1L)).thenReturn(Optional.of(cliente));

        // addetto trovato ma NON attivo
        AddettoVendite addettoNonAttivo = new AddettoVendite();
        addettoNonAttivo.setId(2L);
        addettoNonAttivo.setAttivo(false);

        when(addettoVenditeRepository.findById(2L)).thenReturn(Optional.of(addettoNonAttivo));

        IllegalStateException ex = assertThrows(IllegalStateException.class,
                () -> propostaService.create(dto));

        assertEquals("Operatore non autorizzato", ex.getMessage());
        verify(propostaRepository, never()).save(any());
    }
}