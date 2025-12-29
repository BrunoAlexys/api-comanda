package br.com.apicomanda.security;

import br.com.apicomanda.domain.Admin;
import br.com.apicomanda.domain.RefreshToken;
import br.com.apicomanda.exception.NotFoundException;
import br.com.apicomanda.exception.TokenRefreshException;
import br.com.apicomanda.helpers.ApplicationConstants;
import br.com.apicomanda.repository.EmployeeRepository;
import br.com.apicomanda.repository.RefreshTokenRepository;
import br.com.apicomanda.repository.AdminRepository;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneOffset;
import java.util.Date;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TokenService {
    @Value("${api.security.token.secret}")
    private String secret;
    private SecretKey secretKey;
    private final AdminRepository adminRepository;
    private final EmployeeRepository employeeRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    @Value("${api.security.token.refresh-expiration-ms}")
    private Long refreshExpirationMs;

    @PostConstruct
    public void init() {
        this.secretKey = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public String generateToken(UserSS user) {
        Instant now = Instant.now();
        Instant expirationTime = getExpirationDate();

        var roles = user.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .collect(Collectors.toList());

        return Jwts.builder()
                .issuer(ApplicationConstants.$ISSUER)
                .subject(user.getUsername())
                .claim("roles", roles)
                .issuedAt(Date.from(now))
                .expiration(Date.from(expirationTime))
                .signWith(secretKey)
                .compact();
    }

    public String validatorToken(String token) {
        try {
            Claims claims = Jwts.parser()
                    .verifyWith(secretKey)
                    .build()
                    .parseSignedClaims(token)
                    .getPayload();
            return claims.getSubject();
        } catch (Exception e) {
            return "";
        }
    }

    public RefreshToken createRefreshToken(String userEmail) {
        Admin admin = this.adminRepository.findByEmail(userEmail);

        if (admin != null) {
            RefreshToken refreshToken = RefreshToken.builder()
                    .admin(admin)
                    .employee(null)
                    .expirationDate(Instant.now().plusMillis(refreshExpirationMs))
                    .token(UUID.randomUUID().toString())
                    .build();
            return refreshTokenRepository.save(refreshToken);
        }

        var employee = this.employeeRepository.findByEmailIgnoreCase(userEmail);
        if (employee != null) {
            RefreshToken refreshToken = RefreshToken.builder()
                    .employee(employee)
                    .admin(null)
                    .expirationDate(Instant.now().plusMillis(refreshExpirationMs))
                    .token(UUID.randomUUID().toString())
                    .build();
            return refreshTokenRepository.save(refreshToken);
        }
        throw new NotFoundException("Usuário não encontrado com o email: " + userEmail);
    }

    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.getExpirationDate().isBefore(Instant.now())) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), "Refresh token expirado. Por favor, faça login novamente.");
        }
        return token;
    }

    private Instant getExpirationDate() {
        return LocalDateTime.now().plusHours(2).toInstant(ZoneOffset.of("-03:00"));
    }
}
