package it.autoflow.authentication.service;

import it.autoflow.authentication.utils.PasswordHasher;
import it.autoflow.authentication.dto.*;
import it.autoflow.user.entity.Cliente;
import it.autoflow.user.entity.Ruolo;
import it.autoflow.user.entity.User;
import it.autoflow.user.repository.ClienteRepository;
import it.autoflow.user.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthenticationServiceImpl implements AuthenticationService {

    private final UserRepository userRepository;
    private final ClienteRepository clienteRepository;

    /**
     * Token in memoria:
     *  - sessionTokens: token di login -> userId
     *  - activationTokens: token attivazione -> userId
     *  - passwordResetTokens: token reset password -> userId
     *
     * Per un progetto reale andrebbero su DB,
     * ma per ora vanno benissimo così.
     */
    private final Map<String, Long> sessionTokens = new ConcurrentHashMap<>();
    private final Map<String, Long> activationTokens = new ConcurrentHashMap<>();
    private final Map<String, Long> passwordResetTokens = new ConcurrentHashMap<>();

    public AuthenticationServiceImpl(UserRepository userRepository,
                                     ClienteRepository clienteRepository) {
        this.userRepository = userRepository;
        this.clienteRepository = clienteRepository;
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO request) {
        if (request == null || request.getEmail() == null || request.getPassword() == null) {
            throw new IllegalArgumentException("Credenziali non valide");
        }

        // Usiamo l'email come username (nel DB il campo è 'username')
        Optional<User> optUser = userRepository.findByUsername(request.getEmail());
        User user = optUser.orElseThrow(() -> new IllegalArgumentException("Utente non trovato"));

        if (!user.isAttivo()) {
            throw new IllegalStateException("Account non attivo");
        }

        String hashedInput = PasswordHasher.sha512(request.getPassword());
        if (!hashedInput.equals(user.getPassword())) {
            throw new IllegalArgumentException("Password errata");
        }

        // Genero un token di sessione semplice
        String token = UUID.randomUUID().toString();
        sessionTokens.put(token, user.getId());

        LoginResponseDTO response = new LoginResponseDTO();
        response.setToken(token);
        response.setUserId(user.getId());
        response.setRuolo(user.getRuolo().name());
        return response;
    }

    @Override
    public boolean logout(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }
        return sessionTokens.remove(token) != null;
    }

    @Override
    public void registerCliente(RegisterClienteDTO dto) {
        if (dto == null || dto.getEmail() == null || dto.getPassword() == null) {
            throw new IllegalArgumentException("Dati di registrazione non validi");
        }

        // Controllo che username/email non sia già usato
        if (userRepository.findByUsername(dto.getEmail()).isPresent()) {
            throw new IllegalArgumentException("Email già registrata");
        }

        Cliente cliente = new Cliente();
        // Campi di User
        cliente.setUsername(dto.getEmail());                      // usiamo l'email come username
        cliente.setPassword(PasswordHasher.sha512(dto.getPassword()));
        cliente.setRuolo(Ruolo.CLIENTE);
        cliente.setAttivo(false);                                 // verrà attivato via token

        // Campi specifici Cliente
        cliente.setNome(dto.getNome());
        cliente.setCognome(dto.getCognome());
        cliente.setEmail(dto.getEmail());
        cliente.setTelefono(dto.getTelefono());
        cliente.setIndirizzo(dto.getIndirizzo());
        // codiceFiscale e dataNascita per ora li lasciamo null / TODO

        cliente = clienteRepository.save(cliente);

        // Genero token di attivazione
        String activationToken = UUID.randomUUID().toString();
        activationTokens.put(activationToken, cliente.getId());

        // TODO: invio email con activationToken
        // Per ora non implementiamo l'invio, ma il token è registrato in memoria.
    }

    @Override
    public boolean activateAccount(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        Long userId = activationTokens.remove(token);
        if (userId == null) {
            return false;
        }

        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            return false;
        }

        User user = optUser.get();
        user.setAttivo(true);
        userRepository.save(user);
        return true;
    }

    @Override
    public void requestPasswordReset(String email) {
        if (email == null || email.isBlank()) {
            return;
        }

        Optional<User> optUser = userRepository.findByUsername(email);
        if (optUser.isEmpty()) {
            // Per sicurezza non diciamo se l'utente esiste o no
            return;
        }

        User user = optUser.get();
        String resetToken = UUID.randomUUID().toString();
        passwordResetTokens.put(resetToken, user.getId());

        // TODO: invio email con resetToken
    }

    @Override
    public boolean confirmPasswordReset(PasswordResetConfirmDTO dto) {
        if (dto == null || dto.getToken() == null || dto.getNuovaPassword() == null) {
            return false;
        }

        Long userId = passwordResetTokens.remove(dto.getToken());
        if (userId == null) {
            return false;
        }

        Optional<User> optUser = userRepository.findById(userId);
        if (optUser.isEmpty()) {
            return false;
        }

        User user = optUser.get();
        user.setPassword(PasswordHasher.sha512(dto.getNuovaPassword()));
        userRepository.save(user);

        return true;
    }

    @Override
    public User getUserFromToken(String token) {
        if (token == null || token.isBlank()) {
            return null;
        }

        Long userId = sessionTokens.get(token);
        if (userId == null) {
            return null;
        }

        return userRepository.findById(userId).orElse(null);
    }
}