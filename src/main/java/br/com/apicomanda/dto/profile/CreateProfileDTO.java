package br.com.apicomanda.dto.profile;

import jakarta.validation.constraints.NotBlank;

public record CreateProfileDTO(@NotBlank(message = "O nome do perfil é obrigatorio") String name) {
}
