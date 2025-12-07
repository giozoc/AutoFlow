package it.autoflow.proposal.service;

import it.autoflow.configuration.entity.Configurazione;
import it.autoflow.configuration.repository.ConfigurazioneRepository;
import it.autoflow.proposal.dto.PropostaDTO;
import it.autoflow.proposal.entity.Proposta;
import it.autoflow.proposal.entity.StatoProposta;
import it.autoflow.proposal.repository.PropostaRepository;
import it.autoflow.user.entity.AddettoVendite;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.entity.Ruolo;
import it.autoflow.user.repository.AddettoVenditeRepository;
import it.autoflow.user.repository.ClienteRepository;
import it.autoflow.vehicle.entity.StatoVeicolo;
import it.autoflow.vehicle.entity.Veicolo;
import it.autoflow.vehicle.repository.VeicoloRepository;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di integrazione per TC4 (Creazione Proposta di Vendita) sul SERVICE.
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class PropostaServiceIT {

    @Autowired
    private PropostaService propostaService;

    @Autowired
    private PropostaRepository propostaRepository;

    @Autowired
    private ClienteRepository clienteRepository;

    @Autowired
    private AddettoVenditeRepository addettoVenditeRepository;

    @Autowired
    private ConfigurazioneRepository configurazioneRepository;

    @Autowired
    private VeicoloRepository veicoloRepository;

    // -------------------------------------------------------
    // TC4_01 - Proposta creata correttamente
    // -------------------------------------------------------
    @Test
    void tc4_01_propostaCreataCorrettamente_salvaInDbERestituisceDtoCorretto() {
        // Cliente
        Cliente cliente = new Cliente();
        cliente.setNome("Chiara");
        cliente.setCognome("Verdi");
        cliente.setEmail("chiara.verdi@example.com");
        cliente.setTelefono("3331112222");
        cliente.setIndirizzo("Via Roma 1");
        cliente.setCodiceFiscale("VRDCHR90B02H501U");
        cliente.setDataNascita(java.time.LocalDate.of(1990, 2, 2));
        cliente.setUsername("chiara.verdi@example.com");
        cliente.setPassword("pwd"); // non rilevante qui
        cliente.setRuolo(Ruolo.CLIENTE);
        cliente.setAttivo(true);
        cliente = clienteRepository.save(cliente);

        // Addetto vendite (attivo)
        AddettoVendite addetto = new AddettoVendite();
        addetto.setNome("Luca");
        addetto.setCognome("Bianchi");
        addetto.setMatricola("A001");
        addetto.setEmail("luca.bianchi@example.com");
        addetto.setTelefono("3339998888");
        addetto.setUsername("luca.bianchi@example.com");
        addetto.setPassword("pwd");
        addetto.setRuolo(Ruolo.ADDETTO_VENDITE);
        addetto.setAttivo(true);
        addetto = addettoVenditeRepository.save(addetto);

        // Veicolo DISPONIBILE e visibile
        Veicolo v = new Veicolo();
        v.setMarca("Fiat");
        v.setModello("Panda");
        v.setPrezzoBase(15000.0);
        v.setStato(StatoVeicolo.DISPONIBILE);
        v.setVisibileAlPubblico(true);
        v = veicoloRepository.save(v);

        // Configurazione del cliente su quel veicolo
        Configurazione conf = new Configurazione();
        conf.setCliente(cliente);
        conf.setVeicolo(v);
        conf.setPrezzoBase(15000.0);
        conf.setPrezzoTotale(17000.0);
        conf.setNote("Config base");
        conf.setDataCreazione(LocalDateTime.now());
        conf.setUltimaModifica(LocalDateTime.now());
        conf = configurazioneRepository.save(conf);

        // DTO della proposta
        PropostaDTO dto = new PropostaDTO();
        dto.setClienteId(cliente.getId());
        dto.setAddettoVenditeId(addetto.getId());
        dto.setConfigurazioneId(conf.getId());
        dto.setPrezzoProposta(17000.0);
        dto.setStato(StatoProposta.BOZZA);
        LocalDateTime creazione = LocalDateTime.of(2025, 1, 10, 10, 0);
        LocalDateTime scadenza = LocalDateTime.of(2025, 2, 10, 10, 0);
        dto.setDataCreazione(creazione);
        dto.setDataScadenza(scadenza);
        dto.setNoteCliente("Note cliente");
        dto.setNoteInterne("Note interne");

        // act
        PropostaDTO result = propostaService.create(dto);

        // assert DTO
        assertNotNull(result.getId());
        assertEquals(cliente.getId(), result.getClienteId());
        assertEquals(addetto.getId(), result.getAddettoVenditeId());
        assertEquals(conf.getId(), result.getConfigurazioneId());
        assertEquals(17000.0, result.getPrezzoProposta());
        assertEquals(StatoProposta.BOZZA, result.getStato());
        assertEquals(creazione, result.getDataCreazione());
        assertEquals(scadenza, result.getDataScadenza());
        assertEquals("Note cliente", result.getNoteCliente());
        assertEquals("Note interne", result.getNoteInterne());

        // assert sul DB
        Proposta salvata = propostaRepository.findById(result.getId())
                .orElseThrow(() -> new IllegalStateException("Proposta non trovata dopo create"));

        assertEquals(cliente.getId(), salvata.getCliente().getId());
        assertEquals(addetto.getId(), salvata.getAddettoVendite().getId());
        assertEquals(conf.getId(), salvata.getConfigurazione().getId());
    }

    // -------------------------------------------------------
    // TC4_02 - Cliente non trovato -> EntityNotFoundException("Cliente non trovato")
    // -------------------------------------------------------
    @Test
    void tc4_02_clienteNonTrovato_lanciaEntityNotFoundException() {
        PropostaDTO dto = new PropostaDTO();
        dto.setClienteId(999L);
        dto.setConfigurazioneId(1L);
        dto.setPrezzoProposta(20000.0);
        dto.setStato(StatoProposta.BOZZA);

        EntityNotFoundException ex = assertThrows(
                EntityNotFoundException.class,
                () -> propostaService.create(dto)
        );

        assertEquals("Cliente non trovato", ex.getMessage());
        assertEquals(0, propostaRepository.count());
    }

    // -------------------------------------------------------
    // TC4_03 - Configurazione non valida (non appartiene al cliente) -> IllegalArgumentException
    // -------------------------------------------------------
    @Test
    void tc4_03_configurazioneNonValida_lanciaIllegalArgumentException() {
        // Cliente A
        Cliente clienteA = new Cliente();
        clienteA.setNome("Cliente A");
        clienteA.setCognome("Test");
        clienteA.setEmail("a@example.com");
        clienteA.setUsername("a@example.com");
        clienteA.setPassword("pwd");
        clienteA.setRuolo(Ruolo.CLIENTE);
        clienteA.setAttivo(true);
        clienteA = clienteRepository.save(clienteA);

        // Cliente B
        Cliente clienteB = new Cliente();
        clienteB.setNome("Cliente B");
        clienteB.setCognome("Test");
        clienteB.setEmail("b@example.com");
        clienteB.setUsername("b@example.com");
        clienteB.setPassword("pwd");
        clienteB.setRuolo(Ruolo.CLIENTE);
        clienteB.setAttivo(true);
        clienteB = clienteRepository.save(clienteB);

        // Veicolo ok
        Veicolo v = new Veicolo();
        v.setMarca("Fiat");
        v.setModello("500");
        v.setPrezzoBase(18000.0);
        v.setStato(StatoVeicolo.DISPONIBILE);
        v.setVisibileAlPubblico(true);
        v = veicoloRepository.save(v);

        // Configurazione appartenente a Cliente B
        Configurazione confB = new Configurazione();
        confB.setCliente(clienteB);
        confB.setVeicolo(v);
        confB.setPrezzoBase(18000.0);
        confB.setPrezzoTotale(19000.0);
        confB.setDataCreazione(LocalDateTime.now());
        confB.setUltimaModifica(LocalDateTime.now());
        confB = configurazioneRepository.save(confB);

        PropostaDTO dto = new PropostaDTO();
        dto.setClienteId(clienteA.getId());
        dto.setConfigurazioneId(confB.getId());
        dto.setPrezzoProposta(19000.0);
        dto.setStato(StatoProposta.BOZZA);

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> propostaService.create(dto)
        );

        assertEquals("Configurazione non valida", ex.getMessage());
        assertEquals(0, propostaRepository.count());
    }

    // -------------------------------------------------------
    // TC4_04 - Veicolo non disponibile -> IllegalStateException("Veicolo non disponibile")
    // -------------------------------------------------------
    @Test
    void tc4_04_veicoloNonDisponibile_lanciaIllegalStateException() {
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

        // Veicolo VENDUTO
        Veicolo v = new Veicolo();
        v.setMarca("Fiat");
        v.setModello("Tipo");
        v.setPrezzoBase(20000.0);
        v.setStato(StatoVeicolo.VENDUTO); // non disponibile
        v.setVisibileAlPubblico(true);
        v = veicoloRepository.save(v);

        Configurazione conf = new Configurazione();
        conf.setCliente(cliente);
        conf.setVeicolo(v);
        conf.setPrezzoBase(20000.0);
        conf.setPrezzoTotale(21000.0);
        conf.setDataCreazione(LocalDateTime.now());
        conf.setUltimaModifica(LocalDateTime.now());
        conf = configurazioneRepository.save(conf);

        PropostaDTO dto = new PropostaDTO();
        dto.setClienteId(cliente.getId());
        dto.setConfigurazioneId(conf.getId());
        dto.setPrezzoProposta(21000.0);
        dto.setStato(StatoProposta.BOZZA);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> propostaService.create(dto)
        );

        assertEquals("Veicolo non disponibile", ex.getMessage());
        assertEquals(0, propostaRepository.count());
    }

    // -------------------------------------------------------
    // TC4_05 - Operatore non autorizzato (addetto non attivo) -> IllegalStateException
    // -------------------------------------------------------
    @Test
    void tc4_05_operatoreNonAutorizzato_lanciaIllegalStateException() {
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

        // Addetto NON attivo
        AddettoVendite addetto = new AddettoVendite();
        addetto.setNome("Luca");
        addetto.setCognome("Bianchi");
        addetto.setMatricola("A001");
        addetto.setEmail("luca.bianchi@example.com");
        addetto.setTelefono("3339998888");
        addetto.setUsername("luca.bianchi@example.com");
        addetto.setPassword("pwd");
        addetto.setRuolo(Ruolo.ADDETTO_VENDITE);
        addetto.setAttivo(false); // NON autorizzato
        addetto = addettoVenditeRepository.save(addetto);

        // Veicolo disponibile
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
        conf = configurazioneRepository.save(conf);

        PropostaDTO dto = new PropostaDTO();
        dto.setClienteId(cliente.getId());
        dto.setAddettoVenditeId(addetto.getId());
        dto.setConfigurazioneId(conf.getId());
        dto.setPrezzoProposta(16000.0);
        dto.setStato(StatoProposta.BOZZA);

        IllegalStateException ex = assertThrows(
                IllegalStateException.class,
                () -> propostaService.create(dto)
        );

        assertEquals("Operatore non autorizzato", ex.getMessage());
        assertEquals(0, propostaRepository.count());
    }
}