package br.com.apicomanda.dto.google;

import com.fasterxml.jackson.annotation.JsonProperty;

public record GoogleUserInfoDTO(
        String id,
        String email,
        @JsonProperty("verified_email") boolean verifiedEmail,
        String name,
        @JsonProperty("given_name") String givenName,
        @JsonProperty("family_name") String familyName,
        String picture
) {
}
