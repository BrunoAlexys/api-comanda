package br.com.apicomanda.service;

import br.com.apicomanda.dto.order.CreateOrderDTO;
import br.com.apicomanda.dto.order.KitchenOrderDTO;

import java.util.List;

public interface OrderService {
    void saveOrder(CreateOrderDTO request);
    List<KitchenOrderDTO> fetchOrderByDate(Long userId);
    void updateOrderStatus(Long orderId, String newStatus);
    Double calculateAverageTime(Long userId);
}
