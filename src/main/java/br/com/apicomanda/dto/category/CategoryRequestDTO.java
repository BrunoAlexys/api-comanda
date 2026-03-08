package br.com.apicomanda.dto.category;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record CategoryRequestDTO(@NotBlank(message = "Categoria é obrigatoria") String name,
                                 @NotNull(message = "Id do administrador é obrigatorio") Long adminId) {
}
