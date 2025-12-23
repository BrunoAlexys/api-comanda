package br.com.apicomanda.controller;

import br.com.apicomanda.dto.order.CreateOrderDTO;
import br.com.apicomanda.dto.order.KitchenOrderDTO;
import br.com.apicomanda.helpers.ApplicationConstants;
import br.com.apicomanda.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApplicationConstants.VERSION + "/api/orders")
@RequiredArgsConstructor
@Slf4j
public class OrderController {

    private final OrderService orderService;

    @PostMapping
    public ResponseEntity<Void> createOrder(@RequestBody @Valid CreateOrderDTO request) {
        this.orderService.saveOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @GetMapping("/kitchen/today/{userId}")
    public ResponseEntity<List<KitchenOrderDTO>> fetchOrderByDate(@PathVariable("userId") Long userId) {
        List<KitchenOrderDTO> orders = this.orderService.fetchOrderByDate(userId);
        return ResponseEntity.ok(orders);
    }

    @PatchMapping("/{orderId}/status")
    public ResponseEntity<Void> updateOrderStatus(@PathVariable("orderId") Long orderId, @RequestBody String status) {
        this.orderService.updateOrderStatus(orderId, status);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/kitchen/statistics/average-time/{userId}")
    public ResponseEntity<Double> getAveragePreparationTime(@PathVariable Long userId) {
        Double averageTime = orderService.calculateAverageTime(userId);
        return ResponseEntity.ok(averageTime);
    }
}