package br.com.apicomanda.service.impl;

import br.com.apicomanda.domain.Profile;
import br.com.apicomanda.domain.RefreshToken;
import br.com.apicomanda.domain.User;
import br.com.apicomanda.dto.auth.CredentialRequestDTO;
import br.com.apicomanda.dto.auth.RefreshTokenDTO;
import br.com.apicomanda.dto.auth.TokenResponse;
import br.com.apicomanda.enums.ErrorUserDisableMessages;
import br.com.apicomanda.exception.TokenRefreshException;
import br.com.apicomanda.exception.UserInactiveException;
import br.com.apicomanda.exception.UserUnauthorizedExecption;
import br.com.apicomanda.repository.RefreshTokenRepository;
import br.com.apicomanda.repository.UserRepository;
import br.com.apicomanda.security.TokenService;
import br.com.apicomanda.security.UserSS;
import br.com.apicomanda.service.UserService;
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
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceImplTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private AuthenticationManager authenticationManager;

    @Mock
    private TokenService tokenService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private UserService userService;

    @InjectMocks
    private AuthServiceImpl authService;

    private CredentialRequestDTO credentials;
    private User user;
    private UserSS userSS;
    private RefreshToken refreshToken;
    private final Long accessTokenExpirationMs = 3600000L;

    @BeforeEach
    void setUp() {
        credentials = new CredentialRequestDTO("user@email.com", "password123");

        user = User.builder()
                .id(1L)
                .email(credentials.email())
                .password("encodedPassword")
                .status(true)
                .profiles(List.of(new Profile(1L, "ROLE_USER")))
                .build();

        var authorities = user.getProfiles().stream()
                .map(profile -> new SimpleGrantedAuthority(profile.getName()))
                .collect(Collectors.toList());

        userSS = new UserSS(user.getId(), user.getEmail(), user.getPassword(), authorities, user.isStatus());

        refreshToken = RefreshToken.builder()
                .id(100L)
                .user(user)
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

        when(userRepository.findByEmail(credentials.email())).thenReturn(user);
        when(passwordEncoder.matches(credentials.password(), user.getPassword())).thenReturn(true);
        when(authenticationManager.authenticate(any(UsernamePasswordAuthenticationToken.class))).thenReturn(authentication);

        when(userService.getUserById(user.getId())).thenReturn(user);
        when(tokenService.generateToken(userSS)).thenReturn(expectedAccessToken);
        when(tokenService.createRefreshToken(user.getEmail())).thenReturn(refreshToken);
        doNothing().when(refreshTokenRepository).deleteByUser(user);

        var tokenResponse = authService.login(credentials);

        assertNotNull(tokenResponse);
        assertEquals(expectedAccessToken, tokenResponse.accessToken());
        assertEquals(expectedRefreshTokenString, tokenResponse.refreshToken());
        assertEquals(accessTokenExpirationMs, tokenResponse.expiresIn());

        verify(userRepository, times(1)).findByEmail(credentials.email());
        verify(passwordEncoder, times(1)).matches(credentials.password(), user.getPassword());
        verify(authenticationManager, times(1)).authenticate(any());
        verify(userService, times(1)).getUserById(user.getId());
        verify(tokenService, times(1)).generateToken(userSS);
        verify(refreshTokenRepository, times(1)).deleteByUser(user);
        verify(tokenService, times(1)).createRefreshToken(user.getEmail());
    }

    @Test
    @DisplayName("Deve lançar UserUnauthorizedExecption para senha incorreta")
    void shouldThrowUserUnauthorizedExceptionForIncorrectPassword() {
        when(userRepository.findByEmail(credentials.email())).thenReturn(user);
        when(passwordEncoder.matches(credentials.password(), user.getPassword())).thenReturn(false);

        assertThrows(UserUnauthorizedExecption.class, () -> authService.login(credentials));

        verify(authenticationManager, never()).authenticate(any());
        verify(tokenService, never()).generateToken(any());
    }

    @Test
    @DisplayName("Deve lançar UserUnauthorizedExecption quando o usuário não existe")
    void shouldThrowUserUnauthorizedExceptionForNonExistentUser() {
        when(userRepository.findByEmail(credentials.email())).thenReturn(null);
        when(authenticationManager.authenticate(any())).thenThrow(new UserUnauthorizedExecption("E-mail ou senha incorretos"));

        var exception = assertThrows(UserUnauthorizedExecption.class, () -> authService.login(credentials));

        assertEquals("E-mail ou senha incorretos", exception.getMessage());

        verify(userRepository, times(1)).findByEmail(credentials.email());
        verify(authenticationManager, times(1)).authenticate(any());
    }

    @Test
    @DisplayName("Deve lançar UserInactiveException para usuário desabilitado")
    void shouldThrowUserInactiveExceptionForDisabledUser() {
        when(userRepository.findByEmail(credentials.email())).thenReturn(user);
        when(passwordEncoder.matches(credentials.password(), user.getPassword())).thenReturn(true);

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
        when(tokenService.createRefreshToken(user.getEmail())).thenReturn(newRefreshToken);
        doNothing().when(refreshTokenRepository).delete(refreshToken);

        TokenResponse tokenResponse = authService.refreshToken(refreshTokenRequest);

        assertNotNull(tokenResponse);
        assertEquals(newAccessToken, tokenResponse.accessToken());
        assertEquals(newRefreshToken.getToken(), tokenResponse.refreshToken());

        verify(tokenService, times(2)).findByToken(refreshTokenRequest.refreshToken());
        verify(tokenService, times(1)).verifyExpiration(refreshToken);
        verify(refreshTokenRepository, times(1)).delete(refreshToken);
        verify(tokenService, times(1)).generateToken(any(UserSS.class));
        verify(tokenService, times(1)).createRefreshToken(user.getEmail());
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