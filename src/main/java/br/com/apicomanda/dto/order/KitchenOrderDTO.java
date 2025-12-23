package br.com.apicomanda.dto.order;

import java.util.List;

public record KitchenOrderDTO(
        String id,
        String orderId,
        String table,
        List<String> items,
        String total,
        String time,
        String status,
        String finishedAt
) {
}
