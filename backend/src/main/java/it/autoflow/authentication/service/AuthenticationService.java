package it.autoflow.authentication.service;

import it.autoflow.authentication.dto.*;
import it.autoflow.user.entity.User;

public interface AuthenticationService {

    LoginResponseDTO login(LoginRequestDTO request);

    boolean logout(String token);

    void registerCliente(RegisterClienteDTO dto);

    boolean activateAccount(String token);

    //void requestPasswordReset(String email);

    PasswordResetResultDTO requestPasswordReset(String email);

    boolean confirmPasswordReset(PasswordResetConfirmDTO dto);

    boolean changePasswordAfterReset(PasswordFirstChangeDTO dto);

    User getUserFromToken(String token);
}