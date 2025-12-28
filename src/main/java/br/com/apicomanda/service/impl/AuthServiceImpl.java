package br.com.apicomanda.service.impl;

import br.com.apicomanda.dto.auth.CredentialRequestDTO;
import br.com.apicomanda.dto.auth.RefreshTokenDTO;
import br.com.apicomanda.dto.auth.TokenResponse;
import br.com.apicomanda.enums.ErrorUserDisableMessages;
import br.com.apicomanda.exception.TokenRefreshException;
import br.com.apicomanda.exception.UserInactiveException;
import br.com.apicomanda.exception.UserUnauthorizedExecption;
import br.com.apicomanda.repository.EmployeeRepository;
import br.com.apicomanda.repository.RefreshTokenRepository;
import br.com.apicomanda.repository.AdminRepository;
import br.com.apicomanda.security.TokenService;
import br.com.apicomanda.security.UserSS;
import br.com.apicomanda.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;

import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final AdminRepository adminRepository;
    private final EmployeeRepository employeeRepository;
    private final AuthenticationManager authenticationManager;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${api.security.token.expiration-ms}")
    private Long accessTokenExpirationMs;

    @Override
    @Transactional
    public TokenResponse login(CredentialRequestDTO credentialRequestDTO) {
        try {
            Authentication auth = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(
                            credentialRequestDTO.email(),
                            credentialRequestDTO.password()
                    )
            );

            return getTokenResponse(auth);

        } catch (AuthenticationException e) {
            log.error("Erro na autenticação: {}", e.getMessage());
            if (e.getMessage() != null && ErrorUserDisableMessages.contains(e.getMessage())) {
                throw new UserInactiveException("Usuário inativo!");
            }
            throw new UserUnauthorizedExecption("E-mail ou senha incorretos");
        }
    }

    private TokenResponse getTokenResponse(Authentication auth) {
        var userSS = (UserSS) auth.getPrincipal();
        var accessToken = this.tokenService.generateToken(userSS);

        if (userSS.isEmployee()) {
            handleEmployeeRefreshToken(userSS);
        } else {
            handleUserRefreshToken(userSS);
        }

        var refreshToken = this.tokenService.createRefreshToken(userSS.getUsername());

        return new TokenResponse(accessToken, refreshToken.getToken(), accessTokenExpirationMs);
    }

    private void handleUserRefreshToken(UserSS userSS) {
        var user = this.adminRepository.findById(userSS.getId()).orElseThrow(() -> new UserUnauthorizedExecption("Usuário não encontrado"));
        if (user != null) {
            refreshTokenRepository.deleteByAdmin(user);
        }
    }

    private void handleEmployeeRefreshToken(UserSS userSS) {
        var employee = this.employeeRepository.findById(userSS.getId()).orElseThrow(() -> new UserUnauthorizedExecption("Funcionário não encontrado"));
        if (employee != null) {
            refreshTokenRepository.deleteByEmployee(employee);
        }
    }

    @Override
    @Transactional
    public TokenResponse refreshToken(RefreshTokenDTO request) {
        return tokenService.findByToken(request.refreshToken())
                .map(tokenService::verifyExpiration)
                .map(oldToken -> {
                    String email;
                    Long id;
                    boolean status;
                    var authorities = java.util.Collections.<SimpleGrantedAuthority>emptyList();
                    boolean isEmp = false;

                    if (oldToken.getAdmin() != null) {
                        var u = oldToken.getAdmin();
                        email = u.getEmail();
                        id = u.getId();
                        status = u.isStatus();
                        authorities = u.getProfiles().stream()
                                .map(p -> new SimpleGrantedAuthority(p.getName().toUpperCase()))
                                .collect(Collectors.toList());
                    } else if (oldToken.getEmployee() != null) {
                        var e = oldToken.getEmployee();
                        email = e.getEmail();
                        id = e.getId();
                        status = e.getActive();
                        isEmp = true;
                        authorities = e.getProfiles().stream()
                                .map(p -> new SimpleGrantedAuthority(p.getName().toUpperCase()))
                                .collect(Collectors.toList());
                    } else {
                        throw new TokenRefreshException(request.refreshToken(), "Token órfão (sem usuário).");
                    }

                    UserSS userSS = new UserSS(id, email, "", authorities, status, isEmp);

                    String newAccessToken = tokenService.generateToken(userSS);

                    refreshTokenRepository.delete(oldToken);

                    var newRefreshToken = tokenService.createRefreshToken(email);

                    return new TokenResponse(newAccessToken, newRefreshToken.getToken(), accessTokenExpirationMs);
                })
                .orElseThrow(() -> new TokenRefreshException(request.refreshToken(), "Refresh token não encontrado."));
    }
}