package br.com.apicomanda.security;

import br.com.apicomanda.domain.RefreshToken;
import br.com.apicomanda.domain.User;
import br.com.apicomanda.exception.NotFoundException;
import br.com.apicomanda.exception.TokenRefreshException;
import br.com.apicomanda.repository.RefreshTokenRepository;
import br.com.apicomanda.service.UserService;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TokenServiceTest {

    @Mock
    private UserService userService;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @InjectMocks
    private TokenService tokenService;

    private User user;
    private UserSS userSS;
    private final String testSecret = "my-test-secret-key-that-is-long-enough-for-hs256";

    @BeforeEach
    void setUp() {
        String secret = "aBcDeFgHiJkLmNoPqRsTuVwXyZ123456";
        ReflectionTestUtils.setField(tokenService, "secret", secret);

        tokenService.init();

        ReflectionTestUtils.setField(tokenService, "refreshExpirationMs", 86400000L);

        this.userSS = new UserSS(1L, "test@example.com", "password", Collections.emptyList(), true);

        this.user = User.builder()
                .id(1L)
                .email("test@example.com")
                .password("password")
                .profiles(List.of())
                .status(true)
                .build();
    }


    @Test
    @DisplayName("Deve gerar um token JWT válido para um usuário")
    void generateToken_shouldGenerateValidJwt() {
        String token = tokenService.generateToken(userSS);

        assertThat(token).isNotNull().isNotBlank();

        String subject = Jwts.parser()
                .verifyWith(getSecretKeyForTest())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();

        assertThat(subject).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("Deve retornar o email do usuário ao validar um token válido")
    void validatorToken_shouldReturnUsernameForValidToken() {
        String token = tokenService.generateToken(userSS);

        String subject = tokenService.validatorToken(token);

        assertThat(subject).isEqualTo(user.getEmail());
    }

    @Test
    @DisplayName("Deve retornar uma string vazia ao validar um token inválido (assinatura errada)")
    void validatorToken_shouldReturnEmptyStringForInvalidSignature() {
        String token = tokenService.generateToken(userSS);
        String invalidToken = token.substring(0, token.length() - 1) + "X";

        String subject = tokenService.validatorToken(invalidToken);

        assertThat(subject).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar uma string vazia ao validar um token expirado")
    void validatorToken_shouldReturnEmptyStringForExpiredToken() {
        SecretKey key = Keys.hmacShaKeyFor(testSecret.getBytes(StandardCharsets.UTF_8));
        String expiredToken = Jwts.builder()
                .subject("expired@user.com")
                .issuedAt(Date.from(Instant.now().minusSeconds(3600)))
                .expiration(Date.from(Instant.now().minusSeconds(1800)))
                .signWith(key)
                .compact();

        String subject = tokenService.validatorToken(expiredToken);

        assertThat(subject).isEmpty();
    }

    @Test
    @DisplayName("Deve criar e salvar um RefreshToken quando o usuário existir")
    void createRefreshToken_shouldCreateAndSaveTokenWhenUserExists() {
        when(userService.getUserByEmail(user.getEmail())).thenReturn(user);
        when(refreshTokenRepository.save(any(RefreshToken.class))).thenAnswer(invocation -> invocation.getArgument(0));

        RefreshToken refreshToken = tokenService.createRefreshToken(user.getEmail());

        assertThat(refreshToken).isNotNull();
        assertThat(refreshToken.getUser()).isEqualTo(user);
        assertThat(refreshToken.getToken()).isNotNull();
        assertThat(refreshToken.getExpirationDate()).isAfter(Instant.now());

        verify(userService, times(1)).getUserByEmail(user.getEmail());
        verify(refreshTokenRepository, times(1)).save(any(RefreshToken.class));
    }

    @Test
    @DisplayName("Deve lançar NotFounException ao tentar criar RefreshToken para usuário inexistente")
    void createRefreshToken_shouldThrowNotFoundExceptionWhenUserDoesNotExist() {
        when(userService.getUserByEmail(anyString())).thenReturn(null);

        assertThrows(NotFoundException.class, () -> {
            tokenService.createRefreshToken("nonexistent@user.com");
        });

        verify(refreshTokenRepository, never()).save(any());
    }

    @Test
    @DisplayName("Deve encontrar um RefreshToken pelo seu token string")
    void findByToken_shouldReturnTokenWhenExists() {
        RefreshToken refreshToken = new RefreshToken();
        when(refreshTokenRepository.findByToken("some-token")).thenReturn(Optional.of(refreshToken));

        Optional<RefreshToken> foundToken = tokenService.findByToken("some-token");

        assertThat(foundToken).isPresent().contains(refreshToken);
    }

    @Test
    @DisplayName("Deve retornar Optional vazio se RefreshToken não for encontrado")
    void findByToken_shouldReturnEmptyWhenNotExists() {
        when(refreshTokenRepository.findByToken(anyString())).thenReturn(Optional.empty());

        Optional<RefreshToken> foundToken = tokenService.findByToken("non-existent-token");

        assertThat(foundToken).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar o próprio token se ele não estiver expirado")
    void verifyExpiration_shouldReturnTokenWhenNotExpired() {
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expirationDate(Instant.now().plusSeconds(60))
                .build();

        RefreshToken result = tokenService.verifyExpiration(token);

        assertThat(result).isEqualTo(token);
        verify(refreshTokenRepository, never()).delete(any());
    }

    @Test
    @DisplayName("Deve lançar TokenRefreshException e deletar o token se ele estiver expirado")
    void verifyExpiration_shouldThrowExceptionAndDeletesTokenWhenExpired() {
        RefreshToken token = RefreshToken.builder()
                .token(UUID.randomUUID().toString())
                .expirationDate(Instant.now().minusSeconds(60))
                .build();

        TokenRefreshException exception = assertThrows(TokenRefreshException.class, () -> {
            tokenService.verifyExpiration(token);
        });

        assertThat(exception.getMessage()).contains("Refresh token expirado");

        verify(refreshTokenRepository, times(1)).delete(token);
    }

    private SecretKey getSecretKeyForTest() {
        return (SecretKey) ReflectionTestUtils.getField(tokenService, "secretKey");
    }
}
