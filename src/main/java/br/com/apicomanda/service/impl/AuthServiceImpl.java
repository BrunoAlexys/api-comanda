package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.RefreshToken;
import br.com.apicomanda.domain.User;
import br.com.apicomanda.dto.auth.RefreshTokenDTO;
import br.com.apicomanda.dto.auth.TokenResponse;
import br.com.apicomanda.dto.auth.CredentialRequestDTO;
import br.com.apicomanda.enums.ErrorUserDisableMessages;
import br.com.apicomanda.exception.TokenRefreshException;
import br.com.apicomanda.exception.UserInactiveException;
import br.com.apicomanda.exception.UserUnauthorizedExecption;
import br.com.apicomanda.repository.RefreshTokenRepository;
import br.com.apicomanda.repository.UserRepository;
import br.com.apicomanda.security.TokenService;
import br.com.apicomanda.security.UserSS;
import br.com.apicomanda.service.AuthService;
import br.com.apicomanda.service.UserService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserService userService;

    @Value("${api.security.token.expiration-ms}")
    private Long accessTokenExpirationMs;

    @Override
    @Transactional
    public TokenResponse login(CredentialRequestDTO credentialRequestDTO) {
        try {
            Authentication auth = getAuthencation(credentialRequestDTO);
            return getTokenResponse(auth);
        } catch (AuthenticationException e) {
            log.error(e.getMessage());
            if (ErrorUserDisableMessages.contains(e.getMessage())) {
                throw new UserInactiveException("Usuário inativo!");
            }
            throw new UserUnauthorizedExecption("E-mail ou senha incorretos");
        }
    }

    private TokenResponse getTokenResponse(Authentication auth) {
        var userSS = (UserSS) auth.getPrincipal();
        var accessToken = this.tokenService.generateToken(userSS);

        var user = this.userService.getUserById(userSS.getId());

        if (user != null) {
            refreshTokenRepository.deleteByUser((user));
        }

        var refreshToken = this.tokenService.createRefreshToken(userSS.getUsername());

        return new TokenResponse(accessToken, refreshToken.getToken(), accessTokenExpirationMs);
    }

    private Authentication getAuthencation(CredentialRequestDTO credentialRequestDTO) {
        var user = this.userRepository.findByEmail(credentialRequestDTO.email());
        if (user != null && !passwordEncoder.matches(credentialRequestDTO.password(), user.getPassword())) {
            throw new UserUnauthorizedExecption("E-mail ou senha incorretos");
        }

        var usernamePassword = new UsernamePasswordAuthenticationToken(
                credentialRequestDTO.email(), credentialRequestDTO.password()
        );

        return this.authenticationManager.authenticate(usernamePassword);
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(RefreshTokenDTO request) {
        return tokenService.findByToken(request.refreshToken())
                .map(tokenService::verifyExpiration)
                .map(RefreshToken::getUser)
                .map(user -> {
                    var authorities = user.getProfiles().stream()
                            .map(profile -> new SimpleGrantedAuthority(profile.getName().toUpperCase()))
                            .collect(Collectors.toList());

                    UserSS userSS = new UserSS(user.getId(), user.getEmail(), user.getPassword(), authorities, user.isStatus());

                    String newAccessToken = tokenService.generateToken(userSS);

                    refreshTokenRepository.delete(tokenService.findByToken(request.refreshToken()).get());

                    RefreshToken newRefreshToken = tokenService.createRefreshToken(user.getEmail());

                    log.info("Token de acesso renovado para o usuário: {}", user.getEmail());
                    return new TokenResponse(newAccessToken, newRefreshToken.getToken(), accessTokenExpirationMs);
                })
                .orElseThrow(() -> new TokenRefreshException(request.refreshToken(), "Refresh token não encontrado."));
    }
}
