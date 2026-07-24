package br.com.apicomanda.controller;

import br.com.apicomanda.dto.order.CreateOrderDTO;
import br.com.apicomanda.dto.order.KitchenOrderDTO;
import br.com.apicomanda.dto.order.OrderHistoryResponseDTO;
import br.com.apicomanda.helpers.ApplicationConstants;
import br.com.apicomanda.service.OrderService;
import br.com.apicomanda.service.RedisSequenceService;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping(ApplicationConstants.VERSION + "/api/orders")
@RequiredArgsConstructor
@Slf4j
@Validated
public class OrderController {

    private final OrderService orderService;
    private final RedisSequenceService redisSequenceService;

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

    @GetMapping("/next-number/{adminId}")
    public ResponseEntity<String> getNextOrderNumber(@PathVariable("adminId") Long adminId) {
        String nextNumber = this.redisSequenceService.getNextOrderNumber(adminId);
        return ResponseEntity.ok(nextNumber);
    }

    @GetMapping("/history/{adminId}")
    public ResponseEntity<Page<OrderHistoryResponseDTO>> getOrderHistory(
            @PathVariable("adminId") Long adminId,
            @RequestParam(value = "search", required = false, defaultValue = "") String search,
            @RequestParam(value = "page", defaultValue = "0") @Min(0) int page,
            @RequestParam(value = "size", defaultValue = "10") @Min(1) @Max(50) int size) {

        String usuarioLogado = SecurityContextHolder.getContext().getAuthentication().getName();

        if (!adminId.toString().equals(usuarioLogado)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }

        Page<OrderHistoryResponseDTO> history = orderService.getOrderHistory(adminId, search, page, size);
        return ResponseEntity.ok(history);
    }
}