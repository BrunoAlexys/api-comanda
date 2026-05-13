package br.com.apicomanda.service;

import br.com.apicomanda.dto.order.CreateOrderDTO;
import br.com.apicomanda.dto.order.KitchenOrderDTO;
import br.com.apicomanda.dto.order.OrderHistoryResponseDTO;
import org.springframework.data.domain.Page;

import java.util.List;

public interface OrderService {
    void saveOrder(CreateOrderDTO request);
    List<KitchenOrderDTO> fetchOrderByDate(Long adminId);
    void updateOrderStatus(Long orderId, String newStatus);
    Double calculateAverageTime(Long adminId);
    Page<OrderHistoryResponseDTO> getOrderHistory(Long adminId, String search, int page, int size);
}