package br.com.apicomanda.dto.category;

import jakarta.validation.constraints.NotBlank;

public record CategoryRequestDTO(@NotBlank(message = "Categoria é obrigatoria") String name) {
}
