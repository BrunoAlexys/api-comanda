package br.com.apicomanda.controller;

import br.com.apicomanda.dto.order.CreateOrderDTO;
import br.com.apicomanda.helpers.ApplicationConstants;
import br.com.apicomanda.service.OrderService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

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
}
