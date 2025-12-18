package br.com.apicomanda.dto.menu;

import br.com.apicomanda.domain.Menu;
import br.com.apicomanda.dto.category.CategoryResponseDTO;

import java.math.BigDecimal;

public record MenuResponseDTO(
        Long id,
        String name,
        String description,
        BigDecimal price,
        CategoryResponseDTO category
) {
    public MenuResponseDTO(Menu menu) {
        this(
                menu.getId(),
                menu.getName(),
                menu.getDescription(),
                menu.getPrice(),
                new CategoryResponseDTO(menu.getCategory())
        );
    }
}
