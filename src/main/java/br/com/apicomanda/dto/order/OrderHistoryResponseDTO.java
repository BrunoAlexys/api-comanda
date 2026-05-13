package br.com.apicomanda.dto.order;

public record OrderHistoryResponseDTO(
        Long id,
        String orderNumber,
        String date,
        String time,
        String table,
        String mainItems,
        String additionalComment,
        String totalValue,
        String paymentMethod
) {

}
