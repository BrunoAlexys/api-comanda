package br.com.apicomanda.dto.category;

import br.com.apicomanda.domain.Category;

public record CategoryResponseDTO(Long id, String name) {
    public CategoryResponseDTO(Category category) {
        this(category.getId(), category.getName());
    }
}
