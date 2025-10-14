package br.com.apicomanda.dto.auth;

public record TokenResponse(String accessToken, String refreshToken, Long expiresIn) {
}
