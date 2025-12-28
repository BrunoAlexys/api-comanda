package br.com.apicomanda.dto.employee;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CreateEmployeeDTO(
        @NotBlank(message = "Nome é obrigatório")
        String name,
        @NotBlank(message = "telefone é obrigatório")
        String telephone,
        @NotBlank(message = "Email é obrigatório")
        @Email
        String email,
        @NotBlank(message = "Senha é obrigatória")
        String password,
        @NotNull(message = "Perfil é obrigatório")
        Long idProfile,
        @NotNull(message = "Usuário é obrigatório")
        Long userId
) {
}
