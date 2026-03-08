package br.com.apicomanda.service.impl;

import br.com.apicomanda.dto.auth.CredentialRequestDTO;
import br.com.apicomanda.dto.auth.RefreshTokenDTO;
import br.com.apicomanda.dto.auth.TokenResponse;
import br.com.apicomanda.dto.google.GoogleCodeDTO;
import br.com.apicomanda.dto.google.GoogleTokenResponseDTO;
import br.com.apicomanda.dto.google.GoogleUserInfoDTO;
import br.com.apicomanda.enums.ErrorUserDisableMessages;
import br.com.apicomanda.exception.TokenRefreshException;
import br.com.apicomanda.exception.UserInactiveException;
import br.com.apicomanda.exception.UserUnauthorizedExecption;
import br.com.apicomanda.repository.AdminRepository;
import br.com.apicomanda.repository.EmployeeRepository;
import br.com.apicomanda.repository.RefreshTokenRepository;
import br.com.apicomanda.security.TokenService;
import br.com.apicomanda.security.UserSS;
import br.com.apicomanda.service.AuthService;
import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

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
    private final RestTemplate restTemplate;

    @Value("${api.security.token.expiration-ms}")
    private Long accessTokenExpirationMs;

    @Value("${google.client.id}")
    private String googleClientId;

    @Value("${google.client.secret}")
    private String googleClientSecret;

    @Value("${google.redirect.uri}")
    private String googleRedirectUri;

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
        return getTokenResponse(userSS);
    }

    private void handleUserRefreshToken(UserSS userSS) {
        var user = this.adminRepository.findById(userSS.getId()).orElseThrow(() -> new UserUnauthorizedExecption("Usuário não encontrado"));
        refreshTokenRepository.deleteByAdmin(user);
    }

    private void handleEmployeeRefreshToken(UserSS userSS) {
        var employee = this.employeeRepository.findById(userSS.getId()).orElseThrow(() -> new UserUnauthorizedExecption("Funcionário não encontrado"));
        refreshTokenRepository.deleteByEmployee(employee);
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

    @Override
    @Transactional
    public TokenResponse loginGoogle(GoogleCodeDTO googleCodeDTO) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);

        MultiValueMap<String, String> map = new LinkedMultiValueMap<>();
        map.add("code", googleCodeDTO.code());
        map.add("client_id", googleClientId);
        map.add("client_secret", googleClientSecret);
        map.add("redirect_uri", googleRedirectUri);
        map.add("grant_type", "authorization_code");

        HttpEntity<MultiValueMap<String, String>> request = new HttpEntity<>(map, headers);

        try {
            ResponseEntity<GoogleTokenResponseDTO> response = restTemplate.postForEntity(
                    "https://oauth2.googleapis.com/token",
                    request,
                    GoogleTokenResponseDTO.class
            );

            if (response.getBody() == null || response.getBody().accessToken() == null) {
                throw  new UserUnauthorizedExecption("Falha ao obter token do Google.");
            }

            String accessToken = response.getBody().accessToken();
            GoogleUserInfoDTO googleUser = getGoogleUserInfo(accessToken);

            return processGoogleUser(googleUser);

        } catch (UserUnauthorizedExecption e) {
            throw e;
        } catch (Exception e) {
            log.error("Erro ao autenticar com Google: {}", e.getMessage());
            throw new UserUnauthorizedExecption("Falha na autenticação com o Google.");
        }
    }

    private GoogleUserInfoDTO getGoogleUserInfo(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        HttpEntity<String> entity = new HttpEntity<>(headers);

        ResponseEntity<GoogleUserInfoDTO> response = restTemplate.exchange(
                "https://www.googleapis.com/oauth2/v2/userinfo",
                HttpMethod.GET,
                entity,
                GoogleUserInfoDTO.class
        );

        return response.getBody();
    }


    private TokenResponse processGoogleUser(GoogleUserInfoDTO googleUser) {
        var admin = this.adminRepository.findByEmail(googleUser.email());
        UserSS userSS;

        if (admin.isPresent()) {
            var authorities = admin.get().getProfiles().stream()
                    .map(p -> new SimpleGrantedAuthority(p.getName().toUpperCase())).toList();
            userSS = new UserSS(admin.get().getId(), admin.get().getEmail(), "", authorities, admin.get().isStatus(), false);
        } else {
            var employee = this.employeeRepository.findByEmailIgnoreCase(googleUser.email());

            if (employee.isPresent()) {
                var authorities = employee.get().getProfiles().stream()
                        .map(p -> new SimpleGrantedAuthority(p.getName().toUpperCase())).toList();
                userSS = new UserSS(employee.get().getId(), employee.get().getEmail(), "", authorities, employee.get().getActive(), true);
            } else {
                throw new UserUnauthorizedExecption("Este e-mail Google não possui cadastro no sistema.");
            }
        }

        return generateTokenForUser(userSS);
    }

    private TokenResponse generateTokenForUser(UserSS userSS) {
        return getTokenResponse(userSS);
    }

    private TokenResponse getTokenResponse(UserSS userSS) {
        var accessToken = this.tokenService.generateToken(userSS);
        if (userSS.isEmployee()) {
            handleEmployeeRefreshToken(userSS);
        } else {
            handleUserRefreshToken(userSS);
        }

        var refreshToken = this.tokenService.createRefreshToken(userSS.getUsername());
        return new TokenResponse(accessToken, refreshToken.getToken(), accessTokenExpirationMs);
    }
}