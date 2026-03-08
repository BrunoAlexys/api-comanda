package br.com.apicomanda.dto.auth;

public record CredentialRequestDTO(
        String email,
        String password
) {
}
