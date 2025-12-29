package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Profile;
import br.com.apicomanda.domain.RefreshToken;
import br.com.apicomanda.domain.Admin;
import br.com.apicomanda.dto.auth.CredentialRequestDTO;
import br.com.apicomanda.dto.auth.RefreshTokenDTO;
import br.com.apicomanda.dto.auth.TokenResponse;
import br.com.apicomanda.enums.ErrorUserDisableMessages;
import br.com.apicomanda.exception.TokenRefreshException;
import br.com.apicomanda.exception.UserInactiveException;
import br.com.apicomanda.exception.UserUnauthorizedExecption;
import br.com.apicomanda.repository.RefreshTokenRepository;
import br.com.apicomanda.repository.AdminRepository;
import br.com.apicomanda.security.TokenService;
import br.com.apicomanda.security.UserSS;
import br.com.apicomanda.service.AdminService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

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
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private AdminService adminService;

    @InjectMocks
    private AuthServiceImpl authService;

    private CredentialRequestDTO credentials;
    private Admin admin;
    private UserSS userSS;
    private RefreshToken refreshToken;
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

        ReflectionTestUtils.setField(authService, "accessTokenExpirationMs", accessTokenExpirationMs);
    }


    @Test
    @DisplayName("Deve fazer login com sucesso, gerar access e refresh tokens")
    void shouldLoginSuccessfullyAndReturnAccessAndRefreshTokens() {
        var authentication = new UsernamePasswordAuthenticationToken(userSS, null, userSS.getAuthorities());
        var expectedAccessToken = "jwt.access.token.string";
        var expectedRefreshTokenString = refreshToken.getToken();

        when(adminRepository.findByEmail(credentials.email())).thenReturn(admin);
        when(passwordEncoder.matches(credentials.password(), admin.getPassword())).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(adminService.getAdminById(admin.getId())).thenReturn(admin);
        when(tokenService.generateToken(userSS)).thenReturn(expectedAccessToken);
        when(tokenService.createRefreshToken(admin.getEmail())).thenReturn(refreshToken);
        doNothing().when(refreshTokenRepository).deleteByAdmin(admin);

        var tokenResponse = authService.login(credentials);

        assertNotNull(tokenResponse);
        assertEquals(expectedAccessToken, tokenResponse.accessToken());
        assertEquals(expectedRefreshTokenString, tokenResponse.refreshToken());
        assertEquals(accessTokenExpirationMs, tokenResponse.expiresIn());

        verify(adminRepository, times(1)).findByEmail(credentials.email());
        verify(passwordEncoder, times(1)).matches(credentials.password(), admin.getPassword());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(adminService, times(1)).getAdminById(admin.getId());
        verify(tokenService, times(1)).generateToken(userSS);
        verify(refreshTokenRepository, times(1)).deleteByAdmin(admin);
        verify(tokenService, times(1)).createRefreshToken(admin.getEmail());
    }

    @Test
    @DisplayName("Deve lançar UserUnauthorizedExecption para senha incorreta")
    void shouldThrowUserUnauthorizedExceptionForIncorrectPassword() {
        when(adminRepository.findByEmail(credentials.email())).thenReturn(admin);
        when(passwordEncoder.matches(credentials.password(), admin.getPassword())).thenReturn(false);

        assertThrows(UserUnauthorizedExecption.class, () -> authService.login(credentials));

        verify(authenticationManager, never()).authenticate(any());
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Deve lançar UserUnauthorizedExecption quando o usuário não existe")
    void shouldThrowUserUnauthorizedExceptionForNonExistentUser() {
        when(adminRepository.findByEmail(credentials.email())).thenReturn(null);
        when(authenticationManager.authenticate(any())).thenThrow(new UserUnauthorizedExecption("E-mail ou senha incorretos"));

        var exception = assertThrows(UserUnauthorizedExecption.class, () -> authService.login(credentials));

        assertEquals("E-mail ou senha incorretos", exception.getMessage());

        verify(adminRepository, times(1)).findByEmail(credentials.email());
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    @DisplayName("Deve lançar UserInactiveException para usuário desabilitado")
    void shouldThrowUserInactiveExceptionForDisabledUser() {
        when(adminRepository.findByEmail(credentials.email())).thenReturn(admin);
        when(passwordEncoder.matches(credentials.password(), admin.getPassword())).thenReturn(true);

        String disabledMessage = ErrorUserDisableMessages.USER_IS_DISABLED.getMessage();
        when(authenticationManager.authenticate(any())).thenThrow(new DisabledException(disabledMessage));

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
        verify(tokenService, times(1)).generateToken(any(UserSS.class));
        verify(tokenService, times(1)).createRefreshToken(admin.getEmail());
    }

    @Test
    @DisplayName("Deve lançar TokenRefreshException se o refresh token não for encontrado")
    void shouldThrowExceptionWhenRefreshTokenIsNotFound() {
        var refreshTokenRequest = new RefreshTokenDTO("non.existent.token");
        when(tokenService.findByToken(refreshTokenRequest.refreshToken())).thenReturn(Optional.empty());

        var exception = assertThrows(TokenRefreshException.class, () -> authService.refreshToken(refreshTokenRequest));
        assertTrue(exception.getMessage().contains("Refresh token não encontrado."));

        verify(tokenService, never()).verifyExpiration(any());
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar TokenRefreshException se o refresh token estiver expirado")
    void shouldThrowExceptionWhenRefreshTokenIsExpired() {
        var refreshTokenRequest = new RefreshTokenDTO(refreshToken.getToken());

        when(tokenService.findByToken(refreshTokenRequest.refreshToken())).thenReturn(Optional.of(refreshToken));
        when(tokenService.verifyExpiration(refreshToken)).thenThrow(new TokenRefreshException(refreshToken.getToken(), "Refresh token expirado."));

        var exception = assertThrows(TokenRefreshException.class, () -> authService.refreshToken(refreshTokenRequest));
        assertTrue(exception.getMessage().contains("Refresh token expirado."));

        verify(tokenService, times(1)).findByToken(refreshTokenRequest.refreshToken());
        verify(tokenService, times(1)).verifyExpiration(refreshToken);
        verify(refreshTokenRepository, never()).delete(any());
        verify(tokenService, never()).generateToken(any(UserSS.class));
    }
}