package br.com.apicomanda.dto.order;

import java.util.List;

public record CreateOrderDTO(
        Integer tableNumber,
        List<OrderItemDTO> items,
        List<Long> appliedFeeIds,
        String additionalComment,
        String userId
) {
}
