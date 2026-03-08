package br.com.apicomanda.service;

import br.com.apicomanda.dto.auth.RefreshTokenDTO;
import br.com.apicomanda.dto.auth.TokenResponse;
import br.com.apicomanda.dto.auth.CredentialRequestDTO;
import br.com.apicomanda.dto.google.GoogleCodeDTO;

public interface AuthService {
    TokenResponse login(CredentialRequestDTO credentialRequestDTO);
    TokenResponse refreshToken(RefreshTokenDTO request);
    TokenResponse loginGoogle(GoogleCodeDTO googleCodeDTO);
}
