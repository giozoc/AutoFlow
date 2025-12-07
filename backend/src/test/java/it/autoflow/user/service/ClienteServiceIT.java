package it.autoflow.user.service;

import it.autoflow.authentication.utils.PasswordHasher;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.entity.Ruolo;
import it.autoflow.user.repository.ClienteRepository;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Test di integrazione per TC2 (Profilo Cliente) sul SERVICE.
 * Usa il contesto completo Spring Boot + DB H2 (profilo "test").
 */
@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ClienteServiceIT {

    @Autowired
    private ClienteService clienteService;

    @Autowired
    private ClienteRepository clienteRepository;

    // ------------------------------------------------------------------
    // TC2_01 - Update cliente valido -> profilo aggiornato in DB
    // ------------------------------------------------------------------
    @Test
    void tc2_01_updateClienteValido_aggiornaProfiloInDb() {
        Cliente existing = new Cliente();
        existing.setUsername("chiara.verdi@example.com");
        existing.setPassword(PasswordHasher.sha512("VecchiaPassword1!"));
        existing.setRuolo(Ruolo.CLIENTE);
        existing.setAttivo(true);
        existing.setNome("Chiara");
        existing.setCognome("Verdi");
        existing.setEmail("chiara.verdi@example.com");
        existing.setTelefono("3331112222");
        existing.setIndirizzo("Via Vecchia 1");
        existing.setCodiceFiscale("VRDCHR90A01H501U");
        existing.setDataNascita(LocalDate.of(1990, 1, 1));
        existing = clienteRepository.save(existing);

        Long id = existing.getId();

        Cliente modifiche = new Cliente();
        modifiche.setNome("Chiara Maria");
        modifiche.setCognome("Verdi");
        modifiche.setEmail("chiara.verdi@example.com");
        modifiche.setTelefono("3339998888");
        modifiche.setIndirizzo("Via Nuova 10");
        modifiche.setAttivo(true);
        modifiche.setCodiceFiscale("VRDCHR90A01H501U");
        modifiche.setDataNascita(LocalDate.of(1990, 1, 1));

        clienteService.update(id, modifiche);

        Cliente updated = clienteRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException("Cliente non trovato dopo update"));

        assertEquals("Chiara Maria", updated.getNome());
        assertEquals("Verdi", updated.getCognome());
        assertEquals("chiara.verdi@example.com", updated.getEmail());
        assertEquals("3339998888", updated.getTelefono());
        assertEquals("Via Nuova 10", updated.getIndirizzo());
        assertTrue(updated.isAttivo());

        // username allineato all'email
        assertEquals("chiara.verdi@example.com", updated.getUsername());
        // password NON deve essere cambiata dall'update
        assertEquals(PasswordHasher.sha512("VecchiaPassword1!"), updated.getPassword());
    }

    // ------------------------------------------------------------------
    // TC2_02 - Update cliente inesistente -> IllegalArgumentException
    // ------------------------------------------------------------------
    @Test
    void tc2_02_updateClienteInesistente_lanciaIllegalArgumentException() {
        Cliente modifiche = new Cliente();
        modifiche.setNome("Mario");
        modifiche.setEmail("mario.rossi@example.com");

        IllegalArgumentException ex = assertThrows(
                IllegalArgumentException.class,
                () -> clienteService.update(999L, modifiche)
        );

        assertTrue(ex.getMessage().contains("Cliente non trovato"));
    }

    // ------------------------------------------------------------------
    // TC2_03 - Delete cliente esistente -> true e rimosso dal DB
    // ------------------------------------------------------------------
    @Test
    void tc2_03_deleteClienteEsistente_restituisceTrueERimuoveDaDb() {
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

        boolean deleted = clienteService.delete(c.getId());
        assertTrue(deleted);
        assertFalse(clienteRepository.existsById(c.getId()));
    }

    // ------------------------------------------------------------------
    // TC2_04 - Delete cliente inesistente -> false
    // ------------------------------------------------------------------
    @Test
    void tc2_04_deleteClienteInesistente_restituisceFalse() {
        boolean deleted = clienteService.delete(999L);
        assertFalse(deleted);
    }

    // ------------------------------------------------------------------
    // TC2_05 - findAll su DB con clienti -> lista non vuota
    // ------------------------------------------------------------------
    @Test
    void tc2_05_findAll_conClienti_restituisceListaNonVuota() {
        Cliente c1 = new Cliente();
        c1.setUsername("c1@example.com");
        c1.setPassword(PasswordHasher.sha512("Pass1!"));
        c1.setRuolo(Ruolo.CLIENTE);
        c1.setAttivo(true);
        c1.setNome("C1");
        c1.setCognome("Test");
        c1.setEmail("c1@example.com");
        c1.setIndirizzo("Via 1");
        c1.setCodiceFiscale("CF1");
        c1.setDataNascita(LocalDate.of(1990, 1, 1));

        Cliente c2 = new Cliente();
        c2.setUsername("c2@example.com");
        c2.setPassword(PasswordHasher.sha512("Pass2!"));
        c2.setRuolo(Ruolo.CLIENTE);
        c2.setAttivo(true);
        c2.setNome("C2");
        c2.setCognome("Test");
        c2.setEmail("c2@example.com");
        c2.setIndirizzo("Via 2");
        c2.setCodiceFiscale("CF2");
        c2.setDataNascita(LocalDate.of(1991, 1, 1));

        clienteRepository.save(c1);
        clienteRepository.save(c2);

        List<Cliente> all = clienteService.findAll();
        assertEquals(2, all.size());
    }

    // ------------------------------------------------------------------
    // TC2_06 - findAll su DB vuoto -> lista vuota
    // ------------------------------------------------------------------
    @Test
    void tc2_06_findAll_suDbVuoto_restituisceListaVuota() {
        List<Cliente> all = clienteService.findAll();
        assertTrue(all.isEmpty());
    }

    // ------------------------------------------------------------------
    // TC2_07 - getById cliente esistente -> entità non null
    // ------------------------------------------------------------------
    @Test
    void tc2_07_getById_clienteEsistente_restituisceCliente() {
        Cliente c = new Cliente();
        c.setUsername("c@example.com");
        c.setPassword(PasswordHasher.sha512("Pass!"));
        c.setRuolo(Ruolo.CLIENTE);
        c.setAttivo(true);
        c.setNome("Carlo");
        c.setCognome("Bianchi");
        c.setEmail("c@example.com");
        c.setIndirizzo("Via 3");
        c.setCodiceFiscale("CF3");
        c.setDataNascita(LocalDate.of(1992, 1, 1));
        c = clienteRepository.save(c);

        Cliente found = clienteService.getById(c.getId());
        assertNotNull(found);
        assertEquals("Carlo", found.getNome());
    }

    // ------------------------------------------------------------------
    // TC2_08 - getById cliente inesistente -> null
    // ------------------------------------------------------------------
    @Test
    void tc2_08_getById_clienteInesistente_restituisceNull() {
        Cliente found = clienteService.getById(999L);
        assertNull(found);
    }

    // ------------------------------------------------------------------
    // TC2_09 - create senza campi User -> imposta default (username=email, ruolo, password)
    // ------------------------------------------------------------------
    @Test
    void tc2_09_create_senzaUsernamePasswordRuolo_impostaValoriDiDefault() {
        Cliente nuovo = new Cliente();
        nuovo.setNome("Laura");
        nuovo.setCognome("Neri");
        nuovo.setEmail("laura.neri@example.com");
        nuovo.setTelefono("3337776666");
        nuovo.setIndirizzo("Via Verde 9");
        nuovo.setCodiceFiscale("NRILRA85A01H501X");
        nuovo.setDataNascita(LocalDate.of(1985, 1, 1));
        nuovo.setAttivo(false); // il service lo setta a true

        Cliente saved = clienteService.create(nuovo);

        assertNotNull(saved.getId());
        assertEquals("laura.neri@example.com", saved.getUsername());
        assertEquals(Ruolo.CLIENTE, saved.getRuolo());
        assertTrue(saved.isAttivo());
        assertEquals(PasswordHasher.sha512("Cliente123!"), saved.getPassword());
    }

    // ------------------------------------------------------------------
    // TC2_10 - update modifica solo alcuni campi, gli altri restano invariati
    // ------------------------------------------------------------------
    @Test
    void tc2_10_update_modificaSoloCampiProfiloLasciandoAltriInvariati() {
        Cliente c = new Cliente();
        c.setUsername("pietro.blu@example.com");
        c.setPassword(PasswordHasher.sha512("Pietro123!"));
        c.setRuolo(Ruolo.CLIENTE);
        c.setAttivo(true);
        c.setNome("Pietro");
        c.setCognome("Blu");
        c.setEmail("pietro.blu@example.com");
        c.setTelefono("3331112222");
        c.setIndirizzo("Via Azzurra 1");
        c.setCodiceFiscale("BLUPTR90A01H501Q");
        c.setDataNascita(LocalDate.of(1990, 1, 1));
        c = clienteRepository.save(c);

        Cliente modifiche = new Cliente();
        modifiche.setTelefono("3339990000");
        modifiche.setIndirizzo("Via Azzurra 99");
        modifiche.setEmail("pietro.blu@example.com");
        modifiche.setNome("Pietro");  // invariato
        modifiche.setCognome("Blu");  // invariato
        modifiche.setCodiceFiscale("BLUPTR90A01H501Q");
        modifiche.setDataNascita(LocalDate.of(1990, 1, 1));
        modifiche.setAttivo(true);

        clienteService.update(c.getId(), modifiche);

        Cliente updated = clienteRepository.findById(c.getId()).orElseThrow();

        assertEquals("3339990000", updated.getTelefono());
        assertEquals("Via Azzurra 99", updated.getIndirizzo());
        assertEquals("Pietro", updated.getNome());
        assertEquals("Blu", updated.getCognome());
        // username e password invariati
        assertEquals("pietro.blu@example.com", updated.getUsername());
        assertEquals(PasswordHasher.sha512("Pietro123!"), updated.getPassword());
    }
}