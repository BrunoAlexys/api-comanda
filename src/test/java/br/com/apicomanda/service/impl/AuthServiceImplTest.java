package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Admin;
import br.com.apicomanda.domain.Employee;
import br.com.apicomanda.domain.Profile;
import br.com.apicomanda.domain.RefreshToken;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.client.RestTemplate;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private AdminRepository adminRepository;

    @Mock
    private EmployeeRepository employeeRepository;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private AuthServiceImpl authService;

    private CredentialRequestDTO credentials;
    private Admin admin;
    private UserSS userSS;
    private RefreshToken refreshToken;
    private GoogleCodeDTO googleCodeDTO;
    private GoogleTokenResponseDTO googleTokenResponse;
    private GoogleUserInfoDTO googleUserInfo;

    private final Long accessTokenExpirationMs = 3600000L;

    @BeforeEach
    void setUp() {
        credentials = new CredentialRequestDTO("user@email.com", "password123");

        admin = Admin.builder()
                .id(1L)
                .email(credentials.email())
                .password("encodedPassword")
                .status(true)
                .profiles(List.of(new Profile(1L, "ROLE_USER")))
                .build();

        var authorities = admin.getProfiles().stream()
                .map(profile -> new SimpleGrantedAuthority(profile.getName()))
                .collect(Collectors.toList());

        userSS = new UserSS(admin.getId(), admin.getEmail(), admin.getPassword(), authorities, admin.isStatus(), false);

        refreshToken = RefreshToken.builder()
                .id(100L)
                .admin(admin)
                .token(UUID.randomUUID().toString())
                .expirationDate(Instant.now().plusMillis(86400000))
                .build();

        googleCodeDTO = new GoogleCodeDTO("fake-google-auth-code");
        googleTokenResponse = new GoogleTokenResponseDTO("fake-google-access-token", "3", "Bearer", "scope", 1);
        googleUserInfo = new GoogleUserInfoDTO("12345", "user@email.com", true, "Test User", "Test", "User", "picture-url");

        ReflectionTestUtils.setField(authService, "accessTokenExpirationMs", accessTokenExpirationMs);
        ReflectionTestUtils.setField(authService, "googleClientId", "fake-client-id");
        ReflectionTestUtils.setField(authService, "googleClientSecret", "fake-client-secret");
        ReflectionTestUtils.setField(authService, "googleRedirectUri", "http://localhost:8080");
    }

    @Test
    @DisplayName("Deve fazer login com sucesso, gerar access e refresh tokens")
    void shouldLoginSuccessfullyAndReturnAccessAndRefreshTokens() {
        var authentication = new UsernamePasswordAuthenticationToken(userSS, null, userSS.getAuthorities());
        var expectedAccessToken = "jwt.access.token.string";
        var expectedRefreshTokenString = refreshToken.getToken();

        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);
        when(adminRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        when(tokenService.generateToken(userSS)).thenReturn(expectedAccessToken);
        when(tokenService.createRefreshToken(admin.getEmail())).thenReturn(refreshToken);
        doNothing().when(refreshTokenRepository).deleteByAdmin(admin);

        var tokenResponse = authService.login(credentials);

        assertNotNull(tokenResponse);
        assertEquals(expectedAccessToken, tokenResponse.accessToken());
        assertEquals(expectedRefreshTokenString, tokenResponse.refreshToken());

        verify(authenticationManager, times(1)).authenticate(any());
        verify(adminRepository, times(1)).findById(admin.getId());
        verify(tokenService, times(1)).generateToken(userSS);
        verify(refreshTokenRepository, times(1)).deleteByAdmin(admin);
    }

    @Test
    @DisplayName("Deve lançar UserUnauthorizedExecption para senha incorreta ou usuário inexistente")
    void shouldThrowUserUnauthorizedExceptionForIncorrectPassword() {
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class)))
                .thenThrow(new BadCredentialsException("Bad credentials"));

        UserUnauthorizedExecption exception = assertThrows(UserUnauthorizedExecption.class, () -> authService.login(credentials));

        assertEquals("E-mail ou senha incorretos", exception.getMessage());
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Deve lançar UserInactiveException para usuário desabilitado")
    void shouldThrowUserInactiveExceptionForDisabledUser() {
        String disabledMessage = ErrorUserDisableMessages.USER_IS_DISABLED.getMessage();
        when(authenticationManager.authenticate(any()))
                .thenThrow(new DisabledException(disabledMessage));

        var exception = assertThrows(UserInactiveException.class, () -> authService.login(credentials));
        assertEquals("Usuário inativo!", exception.getMessage());

        verify(tokenService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Deve renovar o token com sucesso usando um refresh token válido")
    void shouldRefreshTokenSuccessfully() {
        var refreshTokenRequest = new RefreshTokenDTO(refreshToken.getToken());
        var newAccessToken = "new.jwt.access.token";
        var newRefreshToken = RefreshToken.builder().token(UUID.randomUUID().toString()).build();

        when(tokenService.findByToken(refreshTokenRequest.refreshToken())).thenReturn(Optional.of(refreshToken));
        when(tokenService.verifyExpiration(refreshToken)).thenReturn(refreshToken);
        when(tokenService.generateToken(any(UserSS.class))).thenReturn(newAccessToken);
        when(tokenService.createRefreshToken(admin.getEmail())).thenReturn(newRefreshToken);
        doNothing().when(refreshTokenRepository).delete(refreshToken);

        TokenResponse tokenResponse = authService.refreshToken(refreshTokenRequest);

        assertNotNull(tokenResponse);
        assertEquals(newAccessToken, tokenResponse.accessToken());
        assertEquals(newRefreshToken.getToken(), tokenResponse.refreshToken());

        verify(tokenService, times(1)).findByToken(refreshTokenRequest.refreshToken());
        verify(tokenService, times(1)).verifyExpiration(refreshToken);
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
        verify(tokenService, times(1)).createRefreshToken(admin.getEmail());
    }

    @Test
    @DisplayName("Deve realizar login Google com sucesso para um Admin existente")
    void shouldLoginGoogleSuccessfullyForAdmin() {
        when(restTemplate.postForEntity(
                eq("https://oauth2.googleapis.com/token"),
                any(HttpEntity.class),
                eq(GoogleTokenResponseDTO.class))
        ).thenReturn(new ResponseEntity<>(googleTokenResponse, HttpStatus.OK));

        when(restTemplate.exchange(
                eq("https://www.googleapis.com/oauth2/v2/userinfo"),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                eq(GoogleUserInfoDTO.class))
        ).thenReturn(new ResponseEntity<>(googleUserInfo, HttpStatus.OK));

        when(adminRepository.findByEmail(googleUserInfo.email())).thenReturn(admin);
        when(adminRepository.findById(admin.getId())).thenReturn(Optional.of(admin));

        String expectedToken = "jwt.google.access.token";
        when(tokenService.generateToken(any(UserSS.class))).thenReturn(expectedToken);
        when(tokenService.createRefreshToken(anyString())).thenReturn(refreshToken);

        TokenResponse response = authService.loginGoogle(googleCodeDTO);

        assertNotNull(response);
        assertEquals(expectedToken, response.accessToken());

        verify(refreshTokenRepository, times(1)).deleteByAdmin(admin);
        verify(employeeRepository, never()).findByEmailIgnoreCase(anyString());
    }

    @Test
    @DisplayName("Deve realizar login Google com sucesso para um Employee existente")
    void shouldLoginGoogleSuccessfullyForEmployee() {
        Employee employee = new Employee();
        employee.setId(2L);
        employee.setEmail(googleUserInfo.email());
        employee.setActive(true);
        employee.setProfiles(List.of(new Profile(2L, "ROLE_EMPLOYEE")));

        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(GoogleTokenResponseDTO.class)))
                .thenReturn(new ResponseEntity<>(googleTokenResponse, HttpStatus.OK));

        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(GoogleUserInfoDTO.class)))
                .thenReturn(new ResponseEntity<>(googleUserInfo, HttpStatus.OK));

        when(adminRepository.findByEmail(googleUserInfo.email())).thenReturn(null);
        when(employeeRepository.findByEmailIgnoreCase(googleUserInfo.email())).thenReturn(employee);
        when(employeeRepository.findById(employee.getId())).thenReturn(Optional.of(employee));

        when(tokenService.generateToken(any(UserSS.class))).thenReturn("jwt.employee.token");
        when(tokenService.createRefreshToken(anyString())).thenReturn(refreshToken);

        TokenResponse response = authService.loginGoogle(googleCodeDTO);

        assertNotNull(response);
        verify(refreshTokenRepository, times(1)).deleteByEmployee(employee);
        verify(adminRepository, times(1)).findByEmail(anyString());
    }

    @Test
    @DisplayName("Deve lançar exceção quando o email do Google não existe no sistema")
    void shouldThrowExceptionWhenGoogleUserNotRegistered() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(GoogleTokenResponseDTO.class)))
                .thenReturn(new ResponseEntity<>(googleTokenResponse, HttpStatus.OK));
        when(restTemplate.exchange(anyString(), eq(HttpMethod.GET), any(HttpEntity.class), eq(GoogleUserInfoDTO.class)))
                .thenReturn(new ResponseEntity<>(googleUserInfo, HttpStatus.OK));

        when(adminRepository.findByEmail(googleUserInfo.email())).thenReturn(null);
        when(employeeRepository.findByEmailIgnoreCase(googleUserInfo.email())).thenReturn(null);

        UserUnauthorizedExecption exception = assertThrows(UserUnauthorizedExecption.class,
                () -> authService.loginGoogle(googleCodeDTO));

        assertEquals("Este e-mail Google não possui cadastro no sistema.", exception.getMessage());

        verify(tokenService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Deve lançar exceção se falhar ao obter token do Google")
    void shouldThrowExceptionWhenGoogleTokenRequestFails() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(GoogleTokenResponseDTO.class)))
                .thenReturn(new ResponseEntity<>(null, HttpStatus.BAD_REQUEST));

        assertThrows(UserUnauthorizedExecption.class, () -> authService.loginGoogle(googleCodeDTO));
    }

    @Test
    @DisplayName("Deve lançar exceção genérica se o RestTemplate falhar")
    void shouldThrowExceptionWhenRestTemplateThrowsError() {
        when(restTemplate.postForEntity(anyString(), any(HttpEntity.class), eq(GoogleTokenResponseDTO.class)))
                .thenThrow(new RuntimeException("Connection Refused"));

        UserUnauthorizedExecption exception = assertThrows(UserUnauthorizedExecption.class,
                () -> authService.loginGoogle(googleCodeDTO));

        assertEquals("Falha na autenticação com o Google.", exception.getMessage());
    }
}