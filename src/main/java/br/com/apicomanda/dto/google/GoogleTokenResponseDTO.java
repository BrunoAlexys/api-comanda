package br.com.apicomanda.dto.google;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleTokenResponseDTO(
        @JsonProperty("access_token") String accessToken,
        @JsonProperty("id_token") String idToken,
        @JsonProperty("scope") String scope,
        @JsonProperty("token_type") String tokenType,
        @JsonProperty("expires_in") Integer expiresIn
) {
}
