package br.com.apicomanda.service;

import br.com.apicomanda.dto.order.CreateOrderDTO;

public interface OrderService {
    void saveOrder(CreateOrderDTO request);
}
