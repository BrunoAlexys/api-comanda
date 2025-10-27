package br.com.apicomanda.service;

import br.com.apicomanda.dto.auth.RefreshTokenDTO;
import br.com.apicomanda.dto.auth.TokenResponse;
import br.com.apicomanda.dto.auth.CredentialRequestDTO;

public interface AuthService {
    TokenResponse login(CredentialRequestDTO credentialRequestDTO);
    TokenResponse refreshToken(RefreshTokenDTO request);
}
