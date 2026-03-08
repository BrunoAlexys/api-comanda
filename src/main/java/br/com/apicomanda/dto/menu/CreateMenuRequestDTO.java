package br.com.apicomanda.dto.menu;

import java.math.BigDecimal;

public record CreateMenuRequestDTO(
        String name,
        String description,
        BigDecimal price,
        Long adminId,
        Long categoryId
) {
}
