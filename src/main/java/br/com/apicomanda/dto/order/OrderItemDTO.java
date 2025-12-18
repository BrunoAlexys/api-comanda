package br.com.apicomanda.dto.order;

public record OrderItemDTO(
        Long menuId,
        Integer quantity
) {
}
